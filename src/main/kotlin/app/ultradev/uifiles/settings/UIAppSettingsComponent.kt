package app.ultradev.uifiles.settings
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import org.jetbrains.annotations.NotNull
import java.io.File
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.io.path.Path
import kotlin.io.path.exists

/**
 * Supports creating and managing a [JPanel] for the Settings Dialog.
 */
class AppSettingsComponent {
    val panel: JPanel
    private val assetZipPathField = TextFieldWithBrowseButton()
    private val hytaleDirectoryText = JBTextField()
    private val patchLineText = JBTextField("release")
    private val buildText = JBTextField("latest")
    private val detectButton = JButton("Detect")

    @get:NotNull
    var assetZipPath: String?
        get() = assetZipPathField.text
        set(newText) {
            assetZipPathField.text = newText ?: ""
        }

    init {
        assetZipPathField.addBrowseFolderListener(
            null,
            FileChooserDescriptorFactory.createSingleFileDescriptor("zip")
                .withTitle("Select Assets.zip")
                .withDescription("Select the Hytale Assets.zip file")
        )

        detectButton.addActionListener {
            assetZipPath = "${hytaleDirectory}/install/${patchLine}/package/game/${build}/Assets.zip"
        }

        // Determine default hytale directory based on system OS.
        if (System.getProperty("os.name").startsWith("Windows")) {
            hytaleDirectoryText.text = "${System.getProperty("user.home")}/AppData/Roaming/Hytale"
        } else if (System.getProperty("os.name").startsWith("Mac")) {
            hytaleDirectoryText.text = "${System.getProperty("user.home")}/Library/Application Support/Hytale"
        } else if (System.getProperty("os.name").startsWith("Linux")) {
            var linuxPath = "${System.getProperty("user.home")}/.var/app/com.hypixel.HytaleLauncher/data/Hytale"
            if (!Path(linuxPath).exists()) {
                linuxPath = "${System.getProperty("user.home")}/.local/share/Hytale"
            }
            hytaleDirectoryText.text = linuxPath
        }

        if (assetZipPath.isNullOrEmpty()) {
            assetZipPath = "${hytaleDirectory}/install/${patchLine}/package/game/${build}/Assets.zip"
        }

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Asset zip path:"), assetZipPathField, 1, false)
            .addSeparator()
            .addLabeledComponent(JBLabel("Hytale directory (for detection):"), hytaleDirectoryText, 1, false)
            .addLabeledComponent(JBLabel("Patch line:"), patchLineText, 1, false)
            .addLabeledComponent(JBLabel("Build:"), buildText, 1, false)
            .addComponent(detectButton)
            .addComponentFillVertically(JPanel(), 0)
            .getPanel()
    }

    val preferredFocusedComponent: JComponent
        get() = assetZipPathField

    var hytaleDirectory: String?
        get() = hytaleDirectoryText.text
        set(newText) {
            hytaleDirectoryText.text = newText
        }

    var patchLine: String?
        get() = patchLineText.text
        set(newText) {
            patchLineText.text = newText
        }

    var build: String?
        get() = buildText.text
        set(newText) {
            buildText.text = newText
        }
}