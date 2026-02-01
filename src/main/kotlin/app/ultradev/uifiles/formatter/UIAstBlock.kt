package app.ultradev.uifiles.formatter

import app.ultradev.hytaleuiparser.ast.AstNode
import app.ultradev.hytaleuiparser.ast.NodeBody
import com.intellij.formatting.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.CodeStyleSettings

class UIAstBlock(
    val node: AstNode,
    private val settings: CodeStyleSettings
) : Block {

    override fun getTextRange(): TextRange {
        val (start, end) = node.textRange
        return TextRange(start, end)
    }

    override fun getSubBlocks(): List<Block> {
        return node.children.map {
            UIAstBlock(it, settings)
        }
    }

    override fun getIndent(): Indent {
        return when {
            isInsideElementBody() -> Indent.getNormalIndent()
            else -> Indent.getNoneIndent()
        }
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return UIFormatterRules.spacing(child1, child2, settings)
    }

    override fun getAlignment(): Alignment? = null
    override fun getWrap(): Wrap? = null
    override fun isLeaf(): Boolean = node.children.isEmpty()

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        return if (node is NodeBody) {
            ChildAttributes(Indent.getNormalIndent(), null)
        } else {
            ChildAttributes(Indent.getNoneIndent(), null)
        }
    }

    override fun isIncomplete(): Boolean {
        return false
    }

    private fun isInsideElementBody(): Boolean {
        val parent = node.parent
        if (parent !is NodeBody) {
            return false
        }

        if (parent.endToken == node) {
            return false
        }

        return true
    }
}
