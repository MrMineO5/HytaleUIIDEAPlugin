package app.ultradev.uifiles.psi

import app.ultradev.hytaleuiparser.ast.AstNode
import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.service.UIAnalysisService
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.findPsiFile
import java.lang.ref.WeakReference
import java.util.WeakHashMap

object UiPsiWrapperFactory {

    private val cache =
        WeakHashMap<AstNode, WeakReference<UiAstNodeFakePsiElement>>()

    fun getOrCreate(
        project: Project,
        node: AstNode
    ): UiAstNodeFakePsiElement? {
        thisLogger().warn("Creating fake PSI for ${node.javaClass.simpleName}")

        cache[node]?.get()?.let { return it }

        val root = node.file
        val relativePath = root.path

        thisLogger().warn("Getting service")
        val service = project.getService(UIAnalysisService::class.java) ?: return null
        thisLogger().warn("Getting virtual file")
        val virtualFile = service.findVirtualFileByRelativePath(relativePath) ?: return null

        thisLogger().warn("Finding psi file")
        val psiFile = virtualFile.findPsiFile(project) as? UIFile ?: return null

        thisLogger().warn("Done")

        val psi = UiAstNodeFakePsiElement(psiFile, node)
        cache[node] = WeakReference(psi)
        return psi
    }

    fun clearCache() {
        cache.clear()
    }
}
