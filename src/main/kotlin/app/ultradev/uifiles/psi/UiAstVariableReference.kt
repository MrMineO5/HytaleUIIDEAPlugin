package app.ultradev.uifiles.psi

import app.ultradev.hytaleuiparser.ast.NodeVariable
import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.ideaTextRange
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult

class UiAstVariableReference(
    file: UIFile, private val astRef: NodeVariable
) : PsiPolyVariantReferenceBase<UIFile>(file, astRef.ideaTextRange, false) {
//    override fun resolve(): PsiElement? {
//        val declAst = astRef.resolvedAssignment ?: return null
//        return UiPsiWrapperFactory.getOrCreate(element.project, declAst)
//    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return astRef.scopes.values.mapNotNull {
            val decl = it.lookupVariableAssignment(astRef.identifier) ?: return@mapNotNull null
            val psi = UiPsiWrapperFactory.getOrCreate(element.project, decl) ?: return@mapNotNull null
            PsiElementResolveResult(psi)
        }.toTypedArray()
    }
}
