package app.ultradev.uifiles

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.impl.source.tree.LeafPsiElement

class UIPsiElement(node: ASTNode) : LeafPsiElement(node.elementType, node.text) {
    override fun toString(): String = "UIPsiElement(${node.elementType})"
}
