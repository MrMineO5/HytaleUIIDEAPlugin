package app.ultradev.uifiles.preview

import app.ultradev.hytaleuiparser.ast.RootNode
import app.ultradev.hytaleuiparser.source.AssetSource
import app.ultradev.uifiles.service.UIAnalysisService
import app.ultradev.uifiles.service.UIPluginDisposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.EventDispatcher
import java.util.EventListener

@Service(Service.Level.PROJECT)
class UIPreviewToolWindowService(project: Project) {

    private val analysisService = project.getService(UIAnalysisService::class.java)
    private val listeners = EventDispatcher.create(Listener::class.java)

    interface Listener : EventListener {
        fun currentFileChanged(file: VirtualFile?)
    }

    @Volatile
    private var currentFile: VirtualFile? = FileEditorManager.getInstance(project)
        .selectedEditor
        ?.file
        ?.takeIf(::isTrackedSelectionFile)

    @Volatile
    private var panel: UIPreviewToolWindowPanel? = null

    init {
        project.messageBus.connect(UIPluginDisposable.getInstance(project)).subscribe(
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
    }

    fun unregisterPanel(panel: UIPreviewToolWindowPanel) {
        if (this.panel == panel) {
            this.panel = null
        }
    }

    fun refreshCurrentFilePreview() {
        val target = panel ?: return
        ApplicationManager.getApplication().invokeLater {
            target.refreshCurrentFilePreview()
        }
    }

    fun getCurrentFile(): VirtualFile? = currentFile

    fun getRootNode(file: VirtualFile): RootNode? = analysisService.getRootNode(file)

    fun getAssetSource(): AssetSource = analysisService.getProjectAssetSource()

    fun addCurrentFileListener(listener: Listener) {
        listeners.addListener(listener)
    }

    fun removeCurrentFileListener(listener: Listener) {
        listeners.removeListener(listener)
    }

    private fun updateCurrentFileIfTracked(file: VirtualFile?) {
        if (!isTrackedSelectionFile(file)) return
        if (currentFile == file) return
        currentFile = file
        listeners.multicaster.currentFileChanged(file)
    }

    private fun isTrackedSelectionFile(file: VirtualFile?): Boolean {
        val extension = file?.extension?.lowercase() ?: return false
        return extension == "ui"
    }
}
