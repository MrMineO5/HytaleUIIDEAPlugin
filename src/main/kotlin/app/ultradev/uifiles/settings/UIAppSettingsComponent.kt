package app.ultradev.uifiles.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import org.jetbrains.annotations.NotNull
import java.awt.event.ActionListener
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Settings UI component for the plugin.
 */
class AppSettingsComponent {
    val panel: JPanel

    // ----------------
    // UI elements
    // ----------------
    private val followConfigCheckbox =
        JBCheckBox("Use assets from Hytale installation", true)

    private val assetZipPathField = TextFieldWithBrowseButton()
    private val hytaleDirectoryField = TextFieldWithBrowseButton()

    private val patchLineBox = ComboBox(
        DefaultComboBoxModel(arrayOf("release", "pre-release"))
    )

    private val buildText = JBTextField("latest")

    // --------------------------
    // Public API
    // --------------------------

    var followConfig: Boolean
        get() = followConfigCheckbox.isSelected
        set(value) {
            followConfigCheckbox.isSelected = value
        }

    @get:NotNull
    var assetZipPath: String?
        get() = assetZipPathField.text
        set(value) {
            assetZipPathField.text = value ?: ""
        }

    var hytaleDirectory: String?
        get() = hytaleDirectoryField.text
        set(value) {
            hytaleDirectoryField.text = value ?: ""
        }

    var patchLine: String?
        get() = patchLineBox.editor.item?.toString()
        set(value) {
            patchLineBox.editor.item = value
        }

    var build: String?
        get() = buildText.text
        set(value) {
            buildText.text = value ?: ""
        }

    val preferredFocusedComponent: JComponent
        get() = patchLineBox

    init {
        // --- File choosers ---

        assetZipPathField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptorFactory
                    .createSingleFileDescriptor("zip")
                    .withDescription("Select Assets.zip")
            )
        )

        hytaleDirectoryField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptorFactory
                    .createSingleFolderDescriptor()
                    .withDescription("Select your Hytale base directory (should contain the 'install' folder)")
            )
        )

        // --- Editable combo box setup ---
        patchLineBox.isEditable = true
        patchLineBox.setMinimumAndPreferredWidth(patchLineBox.preferredSize.width)

        // --- Defaults ---
        hytaleDirectoryField.text = HytaleDirs.defaultHytaleDirectory()
        assetZipPathField.text = computedAssetZipPath()

        // --- Reactive updates ---
        followConfigCheckbox.addActionListener {
            updateAssetZipState()
        }

        val onConfigChanged = ActionListener {
            recomputeIfFollowing()
        }

        patchLineBox.addActionListener(onConfigChanged)
        buildText.document.addChangeListener { recomputeIfFollowing() }
        hytaleDirectoryField.textField.document.addChangeListener { recomputeIfFollowing() }

        // Initial state sync
        updateAssetZipState()

        // --- Layout ---
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Hytale directory:"), hytaleDirectoryField)
            .addLabeledComponent(JBLabel("Patch line:"), patchLineBox)
            .addLabeledComponent(JBLabel("Build:"), buildText)
            .addSeparator()
            .addComponent(followConfigCheckbox)
            .addLabeledComponent(JBLabel("Assets.zip path:"), assetZipPathField)
            .addComponentFillVertically(JPanel(), 0)
            .getPanel()
    }

    // --------------------------
    // Internal logic
    // --------------------------

    private fun computedAssetZipPath(): String = HytaleDirs.defaultAssetsZipPath(
        hytaleDirectory ?: "",
        patchLine ?: "",
        build ?: ""
    )

    private fun updateAssetZipState() {
        val follow = followConfigCheckbox.isSelected
        assetZipPathField.isEnabled = !follow

        if (follow) {
            assetZipPathField.text = computedAssetZipPath()
        }
    }

    private fun recomputeIfFollowing() {
        if (followConfigCheckbox.isSelected) {
            assetZipPathField.text = computedAssetZipPath()
        }
    }
}

/**
 * Tiny helper to reduce Swing boilerplate.
 */
private fun javax.swing.text.Document.addChangeListener(onChange: () -> Unit) {
    addDocumentListener(object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent) = onChange()
        override fun removeUpdate(e: DocumentEvent) = onChange()
        override fun changedUpdate(e: DocumentEvent) = onChange()
    })
}
