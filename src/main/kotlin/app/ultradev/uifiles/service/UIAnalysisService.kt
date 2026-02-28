package app.ultradev.uifiles.service

import app.ultradev.hytaleuiparser.Parser
import app.ultradev.hytaleuiparser.ParserException
import app.ultradev.hytaleuiparser.Tokenizer
import app.ultradev.hytaleuiparser.Validator
import app.ultradev.hytaleuiparser.ast.RootNode
import app.ultradev.hytaleuiparser.source.ArchiveAssetSource
import app.ultradev.hytaleuiparser.source.AssetSource
import app.ultradev.hytaleuiparser.source.CombinedAssetSource
import app.ultradev.uifiles.parser.VirtualFileAssetSource
import app.ultradev.uifiles.preview.UIPreviewToolWindowService
import app.ultradev.uifiles.service.UIError.UIErrorParseRecoverable
import app.ultradev.uifiles.settings.UIAppSettings
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.PsiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.StringReader
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path

@OptIn(FlowPreview::class)
@Service(Service.Level.PROJECT)
class UIAnalysisService(
    private val project: Project,
    private val scope: CoroutineScope,
) : Disposable.Default {
    private val internalAsts = ConcurrentHashMap<VirtualFile, RootNode>()
    private val zipAsts = ConcurrentHashMap<VirtualFile, RootNode>()
    private val asts = ConcurrentHashMap<VirtualFile, RootNode>()
    private val diagnostics = ConcurrentHashMap<VirtualFile, List<UIError>>()

    private val validationLock = Mutex()

    private val validateFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        scope.launch {
            validateFlow
                .debounce(500)
                .collectLatest {
                    validateAll()

                    withContext(Dispatchers.EDT) {
                        dropCachesAndRestartDaemon()
                        refreshPreviewIfOpen()
                    }
                }
        }

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
                    reparse(file, doc)
                }
            },
            this
        )
    }

    private fun listenToVfs() {
        project.messageBus.connect().subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    val index = ProjectRootManager.getInstance(project).fileIndex
                    val files = events.mapNotNull { it.file }.filter(::isUiFile)
                        .filter { index.isInProject(it) }
                    if (files.isEmpty()) return

                    files.forEach { parseFromDisk(it) }
                    validateFlow.tryEmit(Unit)
                }
            }
        )
    }

    private fun reparse(file: VirtualFile, document: Document) {
        parseFromDocument(file, document)
        validateFlow.tryEmit(Unit)
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
            diagnostics[file] = parser.parserErrors.map(::UIErrorParseRecoverable)
        } catch (e: ParserException) {
            internalAsts.remove(file)
            diagnostics[file] = listOf(
                UIError.UIErrorParse(e)
            )
        }
    }

    private fun parseFromZip() {
        val assetZipPath = UIAppSettings.instance.state.effectiveAssetZipPath()
        if (assetZipPath.isEmpty()) return
        val zipFile = Paths.get(assetZipPath).toFile()
        if (!zipFile.exists()) return

        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(zipFile) ?: return
        val jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(virtualFile) ?: return

        zipAsts.clear()
        VfsUtilCore.iterateChildrenRecursively(jarRoot, null) { file ->
            if (file.extension == "ui" && getRelativePath(file).startsWith("Common/UI/Custom")) {
                try {
                    val tokenizer = Tokenizer(StringReader(VfsUtilCore.loadText(file)))
                    val parser = Parser(tokenizer)
                    val ast = parser.finish()
                    zipAsts[file] = ast
                } catch (e: ParserException) {
                    // If the common fails to parse correctly... do we care?
                    //zipAsts.remove(file)
                }
            }
            true
        }
    }

    private suspend fun validateAll() = validationLock.withLock {
        val assetIndex = /*readAction {
            AssetIndex.buildIndex(getProjectAssetSource())
        }*/ null   // TODO: We need to have some level of caching, ideally we can take advantage of IntelliJ's infrastructure

        withContext(Dispatchers.Main) {
            val internalSnapshot = internalAsts.toMap()
            val zipSnapshot = zipAsts.toMap()
            val allAstSnapshot = zipSnapshot + internalSnapshot
            if (allAstSnapshot.isEmpty()) return@withContext

            val pathMap = allAstSnapshot.mapKeys { getRelativePath(it.key) }

            val validator = Validator(pathMap, validateUnusedVariables = true, assetIndex = assetIndex)

            val validatedAsts = mutableMapOf<VirtualFile, RootNode>()
            allAstSnapshot.forEach { (file, ast) ->
                val relativePath = getRelativePath(file)
                try {
                    validator.validateRoot(relativePath)
                    diagnostics.compute(
                        file
                    ) { _, errors ->
                        val newErrors = validator.validationErrors
                            .filter { it.node.file == ast }
                            .map {
                                UIError.UIErrorValidate(it)
                            }
                        errors?.plus(newErrors) ?: newErrors
                    }
                    validatedAsts[file] = ast
                } catch (e: Exception) {
                    thisLogger().error("Could not validate $relativePath:", e)
                }
            }

            asts.clear()
            asts.putAll(validatedAsts)
        }
    }

    private fun dropCachesAndRestartDaemon() {
        ApplicationManager.getApplication().invokeLaterOnWriteThread {
            PsiManager.getInstance(project).let {
                it.dropResolveCaches()
                it.dropPsiCaches()
            }
        }
        DaemonCodeAnalyzer.getInstance(project).restart()
    }

    private fun refreshPreviewIfOpen() {
        project.serviceIfCreated<UIPreviewToolWindowService>()?.refreshCurrentFilePreview()
    }

    private fun isUiFile(file: VirtualFile): Boolean =
        file.extension == "ui" &&
                file.isInLocalFileSystem &&
                ProjectRootManager.getInstance(project).fileIndex.isInSourceContent(file)

    fun getRelativePath(file: VirtualFile): String {
        ProjectRootManager.getInstance(project)
            .fileIndex
            .getSourceRootForFile(file)
            ?.let { root ->
                val rel = VfsUtilCore.getRelativePath(file, root, '/')
                if (rel != null) return rel
            }

        if (file.fileSystem is JarFileSystem) {
            var jarRoot = file
            while (jarRoot.parent != null) {
                jarRoot = jarRoot.parent
            }
            return VfsUtilCore.getRelativePath(file, jarRoot, '/') ?: file.path
        }

        thisLogger().warn("Failed to relativize $file, returning full path")

        return file.path
    }

    fun getDiagnostics(file: VirtualFile): List<UIError> {
        return diagnostics[file] ?: emptyList()
    }

    fun getRootNode(file: VirtualFile): RootNode? {
        return asts[file]
    }

    fun getUnvalidatedRootNode(file: VirtualFile): RootNode? {
        return zipAsts[file] ?: internalAsts[file]
    }

    fun findVirtualFileByRelativePath(relativePath: String): VirtualFile? {
        return asts.keys.find { getRelativePath(it) == relativePath }
    }

    fun getProjectAssetSource(): AssetSource {
        val rootManager = ProjectRootManager.getInstance(project)
        return CombinedAssetSource(
            rootManager.contentSourceRoots.map { VirtualFileAssetSource(it) } +
                    ArchiveAssetSource(Path(UIAppSettings.instance.state.effectiveAssetZipPath()))
        )
    }

    fun scheduleInitialScan() {
        scope.launch {
            val files = readAction {
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
                files
            }

            readAction { parseFromZip() }
            files.forEach { readAction { parseFromDisk(it) } }

            validateAll()

            withContext(Dispatchers.EDT) {
                dropCachesAndRestartDaemon()
                refreshPreviewIfOpen()
            }
        }
    }
}
