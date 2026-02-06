package app.ultradev.uifiles

import app.ultradev.hytaleuiparser.ast.RootNode
import app.ultradev.uifiles.service.UIAnalysisService
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry

class UIFile(viewProvider: FileViewProvider) : PsiFileImpl(UITokenTypes.FILE, UITokenTypes.FILE, viewProvider) {

    fun getRootNode(): RootNode? {
        val file = virtualFile ?: return null
//        val psiManager = PsiDocumentManager.getInstance(project)
//        if (!psiManager.isCommitted(this.fileDocument)) return null

        val service = project.getService(UIAnalysisService::class.java)
        return service?.getRootNode(file)
    }


    override fun getFileType(): FileType = UIFileType.INSTANCE

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
    }

    override fun getReferences(): Array<PsiReference> = ReferenceProvidersRegistry.getReferencesFromProviders(this)

//    override fun findElementAt(offset: Int): PsiElement? {
//        val root = getRootNode() ?: return null
//        val node = root.findNodeAtOffset(offset) ?: return null
//        val finalNode = if (node is NodeToken) node.parent else node
//        return UiPsiWrapperFactory.getOrCreate(project, finalNode)
//    }
}
