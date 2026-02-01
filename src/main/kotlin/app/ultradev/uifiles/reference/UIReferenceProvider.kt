package app.ultradev.uifiles.reference

import app.ultradev.hytaleuiparser.ast.AstNode
import app.ultradev.hytaleuiparser.ast.NodeAssignReference
import app.ultradev.hytaleuiparser.ast.NodeAssignVariable
import app.ultradev.hytaleuiparser.ast.NodeReference
import app.ultradev.hytaleuiparser.ast.NodeVariable
import app.ultradev.hytaleuiparser.ast.visitor.AstVisitor
import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.psi.UiAstImportReference
import app.ultradev.uifiles.psi.UiAstVariableReference
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

class ReferenceCollector : AstVisitor {
    val variableRefs = mutableListOf<NodeVariable>()
    val importRefs = mutableListOf<NodeReference>()

    override fun visit(node: AstNode) {
        if (node is NodeVariable && node.parent !is NodeAssignVariable) variableRefs += node
        if (node is NodeReference && node.parent !is NodeAssignReference) importRefs += node
    }
}

class UIFileReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<PsiReference> {
        thisLogger().debug("Getting references for $element")
        val file = element as? UIFile ?: return PsiReference.EMPTY_ARRAY
        val root = file.getRootNode() ?: return PsiReference.EMPTY_ARRAY
        thisLogger().debug("Root node: $root")

        val refs = mutableListOf<PsiReference>()

        val collector = ReferenceCollector()
        root.walk(collector)

        collector.variableRefs.forEach {
            refs += UiAstVariableReference(file, it, it.textRange.let { (start, end) -> TextRange(start, end) })
        }

        collector.importRefs.forEach {
            refs += UiAstImportReference(file, it, it.textRange.let { (start, end) -> TextRange(start, end) })
        }

        thisLogger().debug("Collected ${refs.size} references: $refs")

        return refs.toTypedArray()
    }
}
