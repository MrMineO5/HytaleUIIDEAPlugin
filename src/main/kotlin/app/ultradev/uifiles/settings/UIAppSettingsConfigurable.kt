package app.ultradev.uifiles.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class UIAppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "Hytale UI Parser Settings"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return mySettingsComponent?.preferredFocusedComponent
    }

    override fun createComponent(): JComponent {
        val component = AppSettingsComponent()
        mySettingsComponent = component
        return component.panel
    }

    override fun isModified(): Boolean {
        val state = UIAppSettings.instance.state
        val ui = mySettingsComponent ?: return false

        return state.followConfig != ui.followConfig ||
                state.assetZipPath != ui.assetZipPath ||
                state.hytaleDirectory != ui.hytaleDirectory ||
                state.patchLine != ui.patchLine ||
                state.build != ui.build
    }

    override fun apply() {
        val state = UIAppSettings.instance.state
        val ui = mySettingsComponent ?: return

        state.followConfig = ui.followConfig
        state.assetZipPath = ui.assetZipPath ?: ""
        state.hytaleDirectory = ui.hytaleDirectory ?: ""
        state.patchLine = ui.patchLine ?: "release"
        state.build = ui.build ?: "latest"
    }

    override fun reset() {
        val state = UIAppSettings.instance.state
        val ui = mySettingsComponent ?: return

        ui.followConfig = state.followConfig
        ui.assetZipPath = state.effectiveAssetZipPath()
        ui.hytaleDirectory = state.effectiveHytaleDirectory()
        ui.patchLine = state.patchLine
        ui.build = state.build
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}
