package app.ultradev.uifiles.preview

import app.ultradev.hytaleuiparser.renderer.HytaleUIPanel
import app.ultradev.hytaleuiparser.renderer.UITransformer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import java.awt.BorderLayout
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class UIPreviewToolWindowPanel(
    project: Project,
    private val toolWindow: ToolWindow
) : JPanel(BorderLayout()) {

    private val previewService = project.getService(UIPreviewToolWindowService::class.java)

    private val previewHost = JPanel(BorderLayout())
    private val emptyLabel = JLabel("Select a .ui file to preview")

    private val cachedPreviewByPath = ConcurrentHashMap<String, JComponent>()
    private var currentPreviewComponent: JComponent? = null

    private val fileListener = object : UIPreviewToolWindowService.Listener {
        override fun currentFileChanged(file: VirtualFile?) {
            renderSelectedFile(file)
        }
    }

    init {
        add(previewHost, BorderLayout.CENTER)
        setPreviewComponent(emptyLabel)
        updateToolWindowTitle(previewService.getCurrentFile(), pending = false)

        val selectedFile = previewService.getCurrentFile()
        renderSelectedFile(selectedFile)
        previewService.addCurrentFileListener(fileListener)
        previewService.registerPanel(this)
    }

    fun setPreviewComponent(component: JComponent?) {
        if (currentPreviewComponent === component) return
        previewHost.removeAll()
        if (component != null) {
            previewHost.add(component, BorderLayout.CENTER)
        }
        currentPreviewComponent = component
        previewHost.revalidate()
        previewHost.repaint()
    }

    fun onContentDisposed() {
        previewService.unregisterPanel(this)
        previewService.removeCurrentFileListener(fileListener)
    }

    fun refreshCurrentFilePreview() {
        val selectedFile = previewService.getCurrentFile()
        renderSelectedFile(selectedFile)
    }

    private fun renderSelectedFile(file: VirtualFile?) {
        if (file == null) {
            setPreviewComponent(emptyLabel)
            updateToolWindowTitle(null, pending = false)
            return
        }

        if (!file.extension.equals("ui", ignoreCase = true)) {
            emptyLabel.text = "Preview currently supports only .ui files"
            setPreviewComponent(emptyLabel)
            updateToolWindowTitle(file, pending = false)
            return
        }

        val rootNode = previewService.getRootNode(file)
        if (rootNode == null) {
            val cached = cachedPreviewByPath[file.path]
            if (cached != null) {
                setPreviewComponent(cached)
                updateToolWindowTitle(file, pending = true)
            } else {
                emptyLabel.text = "No parsed RootNode yet for ${file.name}"
                setPreviewComponent(emptyLabel)
                updateToolWindowTitle(file, pending = true)
            }
            return
        }

        try {
            val rootUIElement = UITransformer.transform(rootNode)
            val panel = HytaleUIPanel(rootUIElement, backgroundImage, previewService.getAssetSource())
            cachedPreviewByPath[file.path] = panel
            setPreviewComponent(panel)
            updateToolWindowTitle(file, pending = false)
        } catch (t: Throwable) {
            val cached = cachedPreviewByPath[file.path]
            if (cached != null) {
                setPreviewComponent(cached)
                updateToolWindowTitle(file, pending = true)
            } else {
                emptyLabel.text = "Failed to render: ${t.message ?: t::class.java.simpleName}"
                setPreviewComponent(emptyLabel)
                updateToolWindowTitle(file, pending = true)
            }
        }
    }

    private fun updateToolWindowTitle(file: VirtualFile?, pending: Boolean) {
        val base = "UI Preview"
        val withFile = if (file == null) base else "$base [${file.name}]"
        toolWindow.setTitle(if (pending) "$withFile Pending..." else withFile)
    }

    companion object {
        private val backgroundImage: BufferedImage by lazy {
            val stream = HytaleUIPanel::class.java.getResourceAsStream("/background.png")
                ?: return@lazy BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
            stream.use { ImageIO.read(it) ?: BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB) }
        }
    }
}
