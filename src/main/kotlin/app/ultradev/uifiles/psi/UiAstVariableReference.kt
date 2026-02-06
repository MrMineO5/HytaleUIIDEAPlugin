package app.ultradev.uifiles.psi

import app.ultradev.hytaleuiparser.ast.NodeVariable
import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.ideaTextRange
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class UiAstVariableReference(
    file: UIFile, private val astRef: NodeVariable
) : PsiReferenceBase<UIFile>(file, astRef.ideaTextRange, false) {
    override fun resolve(): PsiElement? {
        val declAst = astRef.resolvedAssignment ?: return null
        return UiPsiWrapperFactory.getOrCreate(element.project, declAst)
    }
}
