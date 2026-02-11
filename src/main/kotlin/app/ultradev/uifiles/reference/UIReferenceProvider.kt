package app.ultradev.uifiles.reference

import app.ultradev.hytaleuiparser.ast.AstNode
import app.ultradev.hytaleuiparser.ast.NodeAssignReference
import app.ultradev.hytaleuiparser.ast.NodeAssignVariable
import app.ultradev.hytaleuiparser.ast.NodeConstant
import app.ultradev.hytaleuiparser.ast.NodeIdentifier
import app.ultradev.hytaleuiparser.ast.NodeMemberField
import app.ultradev.hytaleuiparser.ast.NodeReference
import app.ultradev.hytaleuiparser.ast.NodeVariable
import app.ultradev.hytaleuiparser.ast.visitor.AstVisitor
import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.psi.UiAstImportPathReference
import app.ultradev.uifiles.psi.UiAstImportReference
import app.ultradev.uifiles.psi.UiAstMemberFieldReference
import app.ultradev.uifiles.psi.UiAstVariableReference
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

class ReferenceCollector : AstVisitor {
    val variableRefs = mutableListOf<NodeVariable>()
    val importRefs = mutableListOf<NodeReference>()
    val importPaths = mutableListOf<NodeConstant>()
    val referenceMembers = mutableListOf<NodeIdentifier>()

    override fun visit(node: AstNode) {
        if (node is NodeVariable) {
            val parent = node.parent
            if (parent is NodeAssignVariable && parent.variable == node) return
            variableRefs += node
        }
        if (node is NodeReference && node.parent !is NodeAssignReference) importRefs += node
        if (node is NodeAssignReference) importPaths += node.filePath!!
        if (node is NodeMemberField && node.valid) referenceMembers += node.member!!
    }
}

class UIFileReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<PsiReference> {
        val file = element as? UIFile ?: return PsiReference.EMPTY_ARRAY
        thisLogger().debug("Getting references for ${file.name}")
        val root = file.getRootNode() ?: return PsiReference.EMPTY_ARRAY
        thisLogger().debug("Found root node")
        thisLogger().trace("Root node: $root")

        val refs = mutableListOf<PsiReference>()

        val collector = ReferenceCollector()
        root.walk(collector)

        collector.variableRefs.forEach {
            refs += UiAstVariableReference(file, it)
        }

        collector.importRefs.forEach {
            refs += UiAstImportReference(file, it)
        }

        collector.importPaths.forEach {
            refs += UiAstImportPathReference(file, it)
        }

        collector.referenceMembers.forEach {
            refs += UiAstMemberFieldReference(file, it)
        }

        thisLogger().debug("Collected ${refs.size} references: $refs")

        return refs.toTypedArray()
    }
}
