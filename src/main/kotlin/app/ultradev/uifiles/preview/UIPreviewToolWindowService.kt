package app.ultradev.uifiles.preview

import app.ultradev.hytaleuiparser.renderer.UITransformer
import app.ultradev.hytaleuiparser.renderer.command.CommandApplicator
import app.ultradev.uifiles.service.UIAnalysisService
import app.ultradev.uifiles.service.UIPluginDisposable
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory

@Service(Service.Level.PROJECT)
class UIPreviewToolWindowService(val project: Project) : Disposable.Default {
    private val analysisService = project.getService(UIAnalysisService::class.java)
    private val debugConnectionService = project.getService(UIDebugConnectionService::class.java)

    @Volatile
    var pinned: Boolean = false
        private set

    @Volatile
    private var currentFile: VirtualFile? = FileEditorManager.getInstance(project)
        .selectedEditor
        ?.file
        ?.takeIf(::isTrackedSelectionFile)

    @Volatile
    private var renderedFile: VirtualFile? = null

    @Volatile
    private var panel: UIPreviewToolWindowPanel? = null

    init {
        project.messageBus.connect(this).subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    updateCurrentFileIfTracked(event.newFile)
                }
            }
        )
    }

    fun registerPanel(panel: UIPreviewToolWindowPanel) {
        this.panel = panel
        panel.updateAssetSource(analysisService.getProjectAssetSource())
        setRenderedFile(renderedFile)
    }

    fun unregisterPanel(panel: UIPreviewToolWindowPanel) {
        if (this.panel == panel) {
            this.panel = null
        }
    }

    fun refreshCurrentFilePreview() {
        ApplicationManager.getApplication().invokeLater {
            setRenderedFile(renderedFile)
        }
    }

    private fun setRenderedFile(file: VirtualFile?) {
        renderedFile = file

        val previewComponent = when (file?.extension?.lowercase()) {
            "ui" -> {
                val rootNode = analysisService.getRootNode(file) ?: return
                UITransformer.transform(rootNode)
            }

            "java", "kt" -> {
                val javaSupportService =
                    ApplicationManager.getApplication().serviceIfCreated<UIPreviewJavaSupportService>()
                        ?: return
                val classNames = javaSupportService.getClassNames(file.findPsiFile(project) ?: return)

                val uiInfo = classNames.firstNotNullOfOrNull { debugConnectionService.getLatestPageInfo(it) }
                    ?: return

                val applicator = CommandApplicator(analysisService.getProjectAssetSource())
                applicator(uiInfo.commands)
            }

            null -> null
            else -> {
                thisLogger().warn("Unsupported file type made it to setRenderedFile: ${file.name}")
                return
            }
        }
        panel?.setPreviewComponent(previewComponent)
    }

    private fun updateCurrentFileIfTracked(file: VirtualFile?) {
        currentFile = file
        if (pinned) return
        if (!isTrackedSelectionFile(file)) return
        setRenderedFile(file)
    }

    private fun isTrackedSelectionFile(file: VirtualFile?): Boolean {
        val extension = file?.extension?.lowercase() ?: return false
        return extension == "ui" || extension == "java" || extension == "kt"
    }

    fun updatePinned(value: Boolean) {
        pinned = value
        if (!pinned) {
            if (isTrackedSelectionFile(currentFile)) {
                setRenderedFile(currentFile)
            }
        }
    }

    fun provideContent(toolWindow: ToolWindow) {
        val panel = UIPreviewToolWindowPanel(project, toolWindow)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        content.setDisposer {
            panel.onContentDisposed()
            unregisterPanel(panel)
        }
        registerPanel(panel)
        toolWindow.contentManager.addContent(content)
    }
}
