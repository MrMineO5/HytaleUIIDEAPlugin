package app.ultradev.uifiles.documentation

import app.ultradev.hytaleuiparser.ast.NodeConstant
import app.ultradev.hytaleuiparser.ast.NodeIdentifier
import app.ultradev.hytaleuiparser.ast.NodeToken
import app.ultradev.hytaleuiparser.ast.NodeTranslation
import app.ultradev.hytaleuiparser.ast.NodeVariable
import app.ultradev.hytaleuiparser.ast.visitor.findNodeAtOffset
import app.ultradev.uifiles.UIFile
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.psi.PsiFile

class UIDocumentationProvider : DocumentationTargetProvider {
    override fun documentationTargets(
        file: PsiFile,
        offset: Int
    ): List<DocumentationTarget> {
        val ui = file as? UIFile ?: return emptyList()
        val root = ui.getRootNode() ?: return emptyList()

        val node = root.findNodeAtOffset(offset) ?: return emptyList()
        val finalNode = if (node is NodeToken) node.parent else node

        thisLogger().warn("Cursor ${offset}, best match: $finalNode")

        if (finalNode is NodeIdentifier || finalNode is NodeVariable) return listOf(UIDocumentationTarget(finalNode))
        if (finalNode is NodeConstant) {
            val parent = finalNode.parent
            if (parent is NodeTranslation) return listOf(UIDocumentationTarget(parent))
        }

        return emptyList()
    }
}
