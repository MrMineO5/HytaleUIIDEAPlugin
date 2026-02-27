package app.ultradev.uifiles.preview.action

import app.ultradev.uifiles.preview.UIDebugConnectionService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class StartUIDebugAction(
    val service: UIDebugConnectionService
) : DumbAwareAction(
    "Start Debug Connection",
    "Start debug connection",
    AllIcons.Actions.Execute
) {
    override fun actionPerformed(e: AnActionEvent) {
        service.start()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = service.canStart()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}