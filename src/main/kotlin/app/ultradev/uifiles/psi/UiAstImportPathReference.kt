package app.ultradev.uifiles.psi

import app.ultradev.hytaleuiparser.ast.NodeAssignReference
import app.ultradev.hytaleuiparser.ast.NodeConstant
import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.ideaTextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class UiAstImportPathReference(
    file: UIFile, private val astRef: NodeConstant
) : PsiReferenceBase<UIFile>(file, astRef.ideaTextRange, false) {
    override fun resolve(): PsiElement? {
        val parent = astRef.parent as? NodeAssignReference ?: return null
        return UiPsiWrapperFactory.getFile(element.project, parent.resolvedFilePath!!)
    }
}
