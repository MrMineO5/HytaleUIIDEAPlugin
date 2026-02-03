package app.ultradev.uifiles.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
final class UIAppSettingsConfigurable : Configurable {

    private var mySettingsComponent: AppSettingsComponent? = null

    // A default constructor with no arguments is required because
    // this implementation is registered as an applicationConfigurable

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "Hytale UI Parser Settings"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return mySettingsComponent?.preferredFocusedComponent
    }

    override fun createComponent(): JComponent? {
        val component = AppSettingsComponent()
        mySettingsComponent = component
        return component.panel
    }

    override fun isModified(): Boolean {
        val state = UIAppSettings.instance?.state ?: return false
        return mySettingsComponent?.assetZipPath != state.assetZipPath
    }

    override fun apply() {
        val state = UIAppSettings.instance?.state ?: return
        state.assetZipPath = mySettingsComponent?.assetZipPath ?: ""
    }

    override fun reset() {
        val state = UIAppSettings.instance?.state ?: return
        mySettingsComponent?.assetZipPath = state.assetZipPath
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}
