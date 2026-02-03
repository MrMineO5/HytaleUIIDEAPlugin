package app.ultradev.uifiles.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.jetbrains.annotations.NonNls
import kotlin.io.path.Path
import kotlin.io.path.exists


@State(
    name = "app.ultradev.uifiles.settings.UIAppSettings",
    storages = [Storage("UIAppPluginSettings.xml")]
)
internal class UIAppSettings
    : PersistentStateComponent<UIAppSettings.State?> {
    class State {
        @NonNls
        var assetZipPath: String = "";
    }

    private var myState: State? = State()

    override fun getState(): State? {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        val instance: UIAppSettings?
            get() = ApplicationManager.getApplication()
                .getService<UIAppSettings?>(UIAppSettings::class.java)
    }
}