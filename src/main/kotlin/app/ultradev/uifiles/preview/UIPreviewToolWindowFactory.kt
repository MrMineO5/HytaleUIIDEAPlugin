package app.ultradev.uifiles.preview

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class UIPreviewToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val service = project.getService(UIPreviewToolWindowService::class.java)
        service.provideContent(toolWindow)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}
