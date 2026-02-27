package app.ultradev.uifiles.preview

import app.ultradev.uifiles.preview.action.StartUIDebugAction
import app.ultradev.uifiles.preview.action.StopUIDebugAction
import com.intellij.icons.AllIcons
import com.intellij.ide.ActivityTracker
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class UIDebugControlPane(
    project: Project,
) : JPanel(FlowLayout(FlowLayout.LEFT, 8, 4)) {
    private val debugConnectionService = project.getService(UIDebugConnectionService::class.java)

    private val debugEndpointField = JTextField("127.0.0.1:14152", 18)
    private val debugStatusLabel = JLabel("Stopped", AllIcons.General.InspectionsPause, JLabel.LEFT)

    private val listener = object : UIDebugConnectionService.Listener {
        override fun statusChanged(status: UIDebugConnectionService.ConnectionStatus) {
            updateStatus(status)
        }
    }

    init {
        add(JLabel("Endpoint"))
        add(debugEndpointField)
        debugEndpointField.addActionListener {
            debugConnectionService.updateEndpoint(debugEndpointField.text)
        }

        val toolbar: ActionToolbar = ActionManager.getInstance().createActionToolbar(
            "UIPreview.DebugToolbar",
            DefaultActionGroup(
                StartUIDebugAction(debugConnectionService),
                StopUIDebugAction(debugConnectionService)
            ),
            true
        )
        toolbar.targetComponent = this
        add(toolbar.component)

        add(debugStatusLabel)
        updateStatus(debugConnectionService.status)
        debugConnectionService.addListener(listener)
    }

    private fun updateStatus(status: UIDebugConnectionService.ConnectionStatus) {
        when (status) {
            UIDebugConnectionService.ConnectionStatus.Stopped -> {
                debugStatusLabel.icon = AllIcons.General.InspectionsPause
                debugStatusLabel.text = "Stopped"
            }

            is UIDebugConnectionService.ConnectionStatus.Connecting -> {
                debugStatusLabel.icon = AllIcons.Process.Step_passive
                debugStatusLabel.text = "Connecting ${status.address}:${status.port}"
            }

            is UIDebugConnectionService.ConnectionStatus.Connected -> {
                debugStatusLabel.icon = AllIcons.General.InspectionsOK
                debugStatusLabel.text = "Connected ${status.address}:${status.port}"
            }

            is UIDebugConnectionService.ConnectionStatus.Disconnected -> {
                debugStatusLabel.icon = AllIcons.General.Warning
                debugStatusLabel.text = "Disconnected ${status.address}:${status.port}"
            }

            is UIDebugConnectionService.ConnectionStatus.Reconnecting -> {
                debugStatusLabel.icon = AllIcons.Actions.Refresh
                debugStatusLabel.text = "Reconnecting #${status.attempt} (${status.delayMs}ms)"
            }
        }
        ActivityTracker.getInstance().inc()
    }

    fun onContentDisposed() {
        debugConnectionService.removeListener(listener)
    }
}