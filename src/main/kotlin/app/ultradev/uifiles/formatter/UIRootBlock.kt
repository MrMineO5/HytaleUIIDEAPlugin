package app.ultradev.uifiles.formatter

import app.ultradev.hytaleuiparser.ast.RootNode
import com.intellij.formatting.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.openapi.util.TextRange

class UIRootBlock(
    private val root: RootNode,
    private val settings: CodeStyleSettings
) : Block {
    override fun getTextRange(): TextRange {
        val (start, end) = root.textRange
        return TextRange(start, end)
    }

    override fun getSubBlocks(): List<Block> {
        return root.children.map { UIAstBlock(it, settings) }
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return UIFormatterRules.spacing(child1, child2, settings)
    }

    override fun getIndent(): Indent? = Indent.getNoneIndent()
    override fun getAlignment(): Alignment? = null
    override fun getWrap(): Wrap? = null
    override fun isLeaf(): Boolean = false

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        return ChildAttributes(Indent.getNoneIndent(), null)
    }

    override fun isIncomplete(): Boolean {
        return false
    }
}
