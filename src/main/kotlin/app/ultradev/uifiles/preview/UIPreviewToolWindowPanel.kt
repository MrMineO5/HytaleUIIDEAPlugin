package app.ultradev.uifiles.preview

import app.ultradev.hytaleuiparser.ast.FakeAstNode
import app.ultradev.hytaleuiparser.generated.elements.GroupProperties
import app.ultradev.hytaleuiparser.renderer.HytaleUIPanel
import app.ultradev.hytaleuiparser.renderer.element.AbstractUIElement
import app.ultradev.hytaleuiparser.renderer.element.impl.UIGroupElement
import app.ultradev.hytaleuiparser.source.AssetSource
import app.ultradev.hytaleuiparser.source.EmptyAssetSource
import app.ultradev.uifiles.preview.action.PinCurrentUIAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import java.awt.BorderLayout
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class UIPreviewToolWindowPanel(
    project: Project,
    private val toolWindow: ToolWindow
) : JPanel(BorderLayout()) {
    private val topPane = JPanel(BorderLayout())
    private val headerControls = JPanel(BorderLayout())
    private val debugControls = UIDebugControlPane(project)
    private val messagePane = JPanel(BorderLayout())

    private val previewHost = JPanel(BorderLayout())
    private val previewComponent = HytaleUIPanel(
        UIGroupElement(FakeAstNode, listOf(), GroupProperties()),
        backgroundImage,
        EmptyAssetSource
    )

    private val emptyLabel = JLabel("Select a .ui file to preview")

    private var currentPreviewComponent: JComponent? = null

    init {
        headerControls.add(debugControls, BorderLayout.WEST)
        topPane.add(headerControls, BorderLayout.NORTH)
        topPane.add(messagePane, BorderLayout.CENTER)

        toolWindow.setTitleActions(listOf(PinCurrentUIAction(project)))


        add(topPane, BorderLayout.NORTH)

        add(previewHost, BorderLayout.CENTER)
        previewHost.add(previewComponent, BorderLayout.CENTER)

        setPreviewMessage(emptyLabel)
    }

    fun setPreviewComponent(root: AbstractUIElement?) {
        previewComponent.replaceElement(root ?: UIGroupElement(FakeAstNode, listOf(), GroupProperties()))
    }

    fun setPreviewMessage(component: JComponent?) {
        if (currentPreviewComponent === component) return
        messagePane.removeAll()
        if (component != null) {
            messagePane.add(component, BorderLayout.CENTER)
        }
        currentPreviewComponent = component
        messagePane.revalidate()
        messagePane.repaint()
    }

    fun onContentDisposed() {
        toolWindow.setTitleActions(emptyList())
        debugControls.onContentDisposed()
    }

    fun updateAssetSource(source: AssetSource) {
        previewComponent.assetSource = source
        previewComponent.context.invalidateCache()
    }

    companion object {
        private val backgroundImage: BufferedImage by lazy {
            val stream = HytaleUIPanel::class.java.getResourceAsStream("/background.png")
                ?: return@lazy BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
            stream.use { ImageIO.read(it) ?: BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB) }
        }
    }
}
