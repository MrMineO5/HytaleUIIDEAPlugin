package app.ultradev.uifiles.preview.action

import app.ultradev.uifiles.preview.UIPreviewToolWindowService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.Project

class PinCurrentUIAction(
    project: Project
) : ToggleAction(
    "Pin Current Preview",
    "Pin current preview (ignore file switching)",
    AllIcons.General.Pin_tab
) {
    private val previewToolService = project.getService(UIPreviewToolWindowService::class.java)

    override fun isSelected(e: AnActionEvent): Boolean = previewToolService.pinned

    override fun setSelected(e: AnActionEvent, state: Boolean) = previewToolService.updatePinned(state)

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}