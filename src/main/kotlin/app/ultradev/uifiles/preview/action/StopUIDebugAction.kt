package app.ultradev.uifiles.preview.action

import app.ultradev.uifiles.preview.UIDebugConnectionService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class StopUIDebugAction(
    val service: UIDebugConnectionService
) : DumbAwareAction(
    "Stop Debug Connection",
    "Stop debug connection",
    AllIcons.Actions.Suspend
) {
    override fun actionPerformed(e: AnActionEvent) {
        service.stop()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = service.canStop()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}