package app.ultradev.uifiles.psi

import app.ultradev.hytaleuiparser.ast.NodeConstant
import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.ideaTextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class UiAstImportPathReference(
    file: UIFile, private val astRef: NodeConstant
) : PsiReferenceBase<UIFile>(file, astRef.ideaTextRange, false) {
    override fun resolve(): PsiElement? {
        return UiPsiWrapperFactory.getFile(element.project, astRef.valueText)
    }
}
