package app.ultradev.uifiles.psi

import app.ultradev.hytaleuiparser.ast.AstNode
import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.service.UIAnalysisService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.findPsiFile
import java.lang.ref.WeakReference
import java.util.WeakHashMap

object UiPsiWrapperFactory {

    private val cache =
        WeakHashMap<AstNode, WeakReference<UiAstPsiElement>>()

    fun getOrCreate(
        project: Project,
        node: AstNode
    ): UiAstPsiElement? {
        cache[node]?.get()?.let { return it }

        val root = node.file
        val relativePath = root.path

        val service = project.getService(UIAnalysisService::class.java) ?: return null
        val virtualFile = service.findVirtualFileByRelativePath(relativePath) ?: return null

        val psiFile = virtualFile.findPsiFile(project) as? UIFile ?: return null

        val psi = UiAstPsiElement(psiFile, node)
        cache[node] = WeakReference(psi)
        return psi
    }

    fun getFile(project: Project, relativePath: String): UIFile? {
        val service = project.getService(UIAnalysisService::class.java) ?: return null
        val virtualFile = service.findVirtualFileByRelativePath(relativePath) ?: return null

        val psiFile = virtualFile.findPsiFile(project) as? UIFile ?: return null
        return psiFile
    }

    fun clearCache() {
        cache.clear()
    }
}
