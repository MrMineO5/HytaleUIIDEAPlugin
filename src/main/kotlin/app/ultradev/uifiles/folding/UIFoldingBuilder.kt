package app.ultradev.uifiles.folding

import app.ultradev.hytaleuiparser.ast.AstNode
import app.ultradev.hytaleuiparser.ast.NodeBody
import app.ultradev.uifiles.UIFile
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class UIFoldingBuilder : FoldingBuilderEx() {

    override fun buildFoldRegions(
        root: PsiElement,
        document: Document,
        quick: Boolean
    ): Array<FoldingDescriptor> {
        val file = root.containingFile as? UIFile ?: return emptyArray()
        val rootNode = file.getUnvalidatedRootNode() ?: return emptyArray()

        val descriptors = mutableListOf<FoldingDescriptor>()

        // Recursively find all foldable regions
        collectFoldableRegions(file, rootNode, document, descriptors)

        return descriptors.toTypedArray()
    }

    private fun collectFoldableRegions(
        file: UIFile,
        node: AstNode,
        document: Document,
        descriptors: MutableList<FoldingDescriptor>
    ) {
        // Fold element bodies that have multiple children or are large enough
        if (node is NodeBody) {
            val (start, end) = node.textRange
            val range = TextRange(start+1, end-1)
            
            // Only create folding region if the body spans multiple lines and has content
            if (range.length > 0 && isMultiLine(range, document)) {
                descriptors.add(
                    FoldingDescriptor(
                        file.node,
                        range,
                        null,
                        " ... ",
                        false,
                        emptySet()
                    )
                )
            }
        }

        // Recursively process children
        node.children.forEach { child ->
            collectFoldableRegions(file, child, document, descriptors)
        }
    }

    private fun isMultiLine(range: TextRange, document: Document): Boolean {
        val startLine = document.getLineNumber(range.startOffset)
        val endLine = document.getLineNumber(range.endOffset)
        return endLine > startLine
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
