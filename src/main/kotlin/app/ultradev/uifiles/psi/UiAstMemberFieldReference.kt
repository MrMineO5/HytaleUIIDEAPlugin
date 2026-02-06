package app.ultradev.uifiles.psi

import app.ultradev.hytaleuiparser.ast.NodeAssignReference
import app.ultradev.hytaleuiparser.ast.NodeConstant
import app.ultradev.hytaleuiparser.ast.NodeIdentifier
import app.ultradev.hytaleuiparser.ast.NodeMemberField
import app.ultradev.hytaleuiparser.ast.NodeType
import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.ideaTextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class UiAstMemberFieldReference(
    file: UIFile, private val astRef: NodeIdentifier
) : PsiReferenceBase<UIFile>(file, astRef.ideaTextRange, false) {
    override fun resolve(): PsiElement? {
        val parent = astRef.parent as? NodeMemberField ?: return null
        val ownerType = parent.ownerAsVariableReference.deepResolve() as? NodeType ?: return null
        val resFields = ownerType.resolveFields()
        val declaration = resFields[astRef.identifier] ?: return null
        return UiPsiWrapperFactory.getOrCreate(element.project, declaration)
    }
}
