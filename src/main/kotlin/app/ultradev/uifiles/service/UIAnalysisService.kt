package app.ultradev.uifiles.service

import app.ultradev.hytaleuiparser.*
import app.ultradev.hytaleuiparser.ast.RootNode
import app.ultradev.uifiles.ideaTextRange
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.PsiManager
import com.intellij.util.Alarm
import com.intellij.util.concurrency.AppExecutorUtil
import java.io.StringReader
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class UIAnalysisService(private val project: Project) {
    private val internalAsts = ConcurrentHashMap<VirtualFile, RootNode>()
    private val asts = ConcurrentHashMap<VirtualFile, RootNode>()
    private val diagnostics = ConcurrentHashMap<VirtualFile, List<UIError>>()

    sealed interface UIError {
        val range: TextRange
    }

    data class UIErrorParse(val error: ParserException) : UIError {
        override val range: TextRange get() = error.token.ideaTextRange
    }

    data class UIErrorValidate(val error: ValidatorError) : UIError {
        override val range: TextRange get() = error.node.ideaTextRange
    }

    private val modificationStamps = ConcurrentHashMap<VirtualFile, Long>()

    private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, project)

    init {
        listenToVfs()
        listenToDocuments()
    }

    private fun listenToDocuments() {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(
            object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) {
                    val doc = event.document
                    val file = FileDocumentManager.getInstance().getFile(doc) ?: return
                    if (!isUiFile(file)) return
                    scheduleReparse(file, doc)
                }
            },
            project
        )
    }

    private fun listenToVfs() {
        project.messageBus.connect().subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    val files = events.mapNotNull { it.file }.filter(::isUiFile)
                    if (files.isEmpty()) return

                    ReadAction.nonBlocking(Callable<Unit> {
                        files.forEach { parseFromDisk(it) }
                        validateAllInternal()
                    })
                        .finishOnUiThread(ModalityState.any()) {
                            dropCachesAndRestartDaemon()
                        }
                        .submit(AppExecutorUtil.getAppExecutorService())
                }
            }
        )
    }

    private fun scheduleReparse(file: VirtualFile, document: Document) {
        alarm.cancelAllRequests()
        alarm.addRequest({
            val stamp = document.modificationStamp
            modificationStamps[file] = stamp

            ReadAction.nonBlocking {
                parseFromDocument(file, document)
                validateAllInternal()
            }
                .finishOnUiThread(ModalityState.any()) {
                    if (modificationStamps[file] != stamp) {
                        thisLogger().warn("Skipping finish")
                        return@finishOnUiThread
                    }
                    dropCachesAndRestartDaemon()
                }
                .submit(AppExecutorUtil.getAppExecutorService())
        }, 250)
    }

    private fun parseFromDisk(file: VirtualFile) {
        parse(file, VfsUtilCore.loadText(file))
    }

    private fun parseFromDocument(file: VirtualFile, document: Document) {
        parse(file, document.text)
    }

    private fun parse(file: VirtualFile, text: String) {
        try {
            val tokenizer = Tokenizer(StringReader(text))
            val parser = Parser(tokenizer)
            internalAsts[file] = parser.finish()
            diagnostics.remove(file)
        } catch (e: ParserException) {
            internalAsts.remove(file)
            diagnostics[file] = listOf(
                UIErrorParse(e)
            )
        }
//        catch (e: Exception) {
//            internalAsts.remove(file)
//            diagnostics[file] = listOf(
//                UIError(TextRange(0, minOf(100, text.length)), "Parse error: ${e.message}")
//            )
//        }
    }

    private fun validateAllInternal() {
        val astSnapshot = internalAsts.toMap()
        if (astSnapshot.isEmpty()) return

        val pathMap = astSnapshot.mapKeys { getRelativePath(it.key) }
        val validator = Validator(pathMap, validateUnusedVariables = true)

        val validatedAsts = mutableMapOf<VirtualFile, RootNode>()
        astSnapshot.forEach { (file, ast) ->
            validator.validateRoot(getRelativePath(file))
            diagnostics.putIfAbsent(
                file, validator.validationErrors
                .filter { it.node.file == ast }
                .map {
                    UIErrorValidate(it)
                }
            )
            validatedAsts[file] = ast
        }
        asts.clear()
        asts.putAll(validatedAsts)
    }

    private fun dropCachesAndRestartDaemon() {
        ApplicationManager.getApplication().invokeLaterOnWriteThread {
            PsiManager.getInstance(project).let {
                it.dropResolveCaches()
                it.dropPsiCaches()
            }
        }
        DaemonCodeAnalyzer.getInstance(project).restart("UI AST updated")
    }

    private fun isUiFile(file: VirtualFile): Boolean =
        file.extension == "ui" &&
                file.isInLocalFileSystem &&
                ProjectRootManager.getInstance(project).fileIndex.isInSourceContent(file)

    private fun getRelativePath(file: VirtualFile): String =
        ProjectRootManager.getInstance(project)
            .fileIndex
            .getSourceRootForFile(file)
            ?.let { root ->
                root.toNioPath().relativize(file.toNioPath()).toString().replace('\\', '/')
            }
            ?: file.path

    fun getDiagnostics(file: VirtualFile): List<UIError> {
        return diagnostics[file] ?: emptyList()
    }

    fun getRootNode(file: VirtualFile): RootNode? {
        return asts[file]
    }

    fun findVirtualFileByRelativePath(relativePath: String): VirtualFile? {
        return asts.keys.find { getRelativePath(it) == relativePath }
    }

    fun scheduleInitialScan() {
        ReadAction.nonBlocking {
            val rootManager = ProjectRootManager.getInstance(project)
            val fileIndex = rootManager.fileIndex

            val files = mutableListOf<VirtualFile>()

            rootManager.contentRootsFromAllModules.forEach { root ->
                VfsUtilCore.iterateChildrenRecursively(root, null) { file ->
                    if (file.extension == "ui" &&
                        file.isInLocalFileSystem &&
                        fileIndex.isInSourceContent(file)
                    ) {
                        files += file
                    }
                    true
                }
            }

            if (files.isEmpty()) return@nonBlocking

            files.forEach { parseFromDisk(it) }
            validateAllInternal()
        }
            .finishOnUiThread(ModalityState.any()) {
                dropCachesAndRestartDaemon()
            }
            .submit(AppExecutorUtil.getAppExecutorService())
    }
}
