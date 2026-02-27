package app.ultradev.uifiles.typing

import app.ultradev.uifiles.UITokenTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class UIPairedBraceMatcher : PairedBraceMatcher {
    companion object {
        val PAIRS = arrayOf(
            BracePair(UITokenTypes.START_PARENTHESIS, UITokenTypes.END_PARENTHESIS, false),
            BracePair(UITokenTypes.START_ELEMENT, UITokenTypes.END_ELEMENT, true),
        )
    }

    override fun getPairs() = PAIRS

    override fun isPairedBracesAllowedBeforeType(
        lbraceType: IElementType,
        contextType: IElementType?
    ): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }
}