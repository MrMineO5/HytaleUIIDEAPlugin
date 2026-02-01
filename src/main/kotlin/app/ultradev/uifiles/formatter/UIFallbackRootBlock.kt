package app.ultradev.uifiles.formatter

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings

class UIFallbackRootBlock(
    private val file: PsiFile,
    private val settings: CodeStyleSettings
) : Block {

    override fun getTextRange(): TextRange = file.textRange

    override fun getSubBlocks(): List<Block> = emptyList()

    override fun getSpacing(child1: Block?, child2: Block): Spacing? = null

    override fun getIndent(): Indent? = Indent.getNoneIndent()
    override fun getAlignment(): Alignment? = null
    override fun getWrap(): Wrap? = null
    override fun isLeaf(): Boolean = true

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes =
        ChildAttributes(Indent.getNoneIndent(), null)

    override fun isIncomplete(): Boolean {
        return true
    }
}
