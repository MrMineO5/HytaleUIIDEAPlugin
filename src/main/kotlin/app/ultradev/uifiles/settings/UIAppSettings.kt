package app.ultradev.uifiles.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.jetbrains.annotations.NonNls


@State(
    name = "app.ultradev.uifiles.settings.UIAppSettings",
    storages = [Storage("UIAppPluginSettings.xml")]
)
internal class UIAppSettings : PersistentStateComponent<UIAppSettings.State> {

    data class State(
        var followConfig: Boolean = true,
        @field:NonNls var assetZipPath: String = "",
        @field:NonNls var hytaleDirectory: String = "",
        @field:NonNls var patchLine: String = "release",
        @field:NonNls var build: String = "latest",
    ) {
        fun effectiveHytaleDirectory(): String {
            if (hytaleDirectory.isNotBlank()) {
                return hytaleDirectory
            }
            return HytaleDirs.defaultHytaleDirectory()
        }

        fun effectiveAssetZipPath(): String {
            return if (followConfig || assetZipPath.isBlank()) {
                HytaleDirs.defaultAssetsZipPath(effectiveHytaleDirectory(), patchLine, build)
            } else {
                assetZipPath
            }
        }
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        val instance: UIAppSettings
            get() = ApplicationManager.getApplication()
                .getService(UIAppSettings::class.java)
    }
}
