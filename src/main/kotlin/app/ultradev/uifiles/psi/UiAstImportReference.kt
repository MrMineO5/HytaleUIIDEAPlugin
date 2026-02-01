package app.ultradev.uifiles.psi

import app.ultradev.hytaleuiparser.ast.NodeReference
import app.ultradev.hytaleuiparser.ast.NodeVariable
import app.ultradev.uifiles.UIFile
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class UiAstImportReference(
    file: UIFile, private val astRef: NodeReference, rangeInFile: TextRange
) : PsiReferenceBase<UIFile>(file, rangeInFile, false) {
    override fun resolve(): PsiElement? {
        val declAst = astRef.resolvedAssignment ?: return null
        return UiPsiWrapperFactory.getOrCreate(element.project, declAst)
    }
}
