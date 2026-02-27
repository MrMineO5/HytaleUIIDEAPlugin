package app.ultradev.uifiles.typing

import app.ultradev.uifiles.UITokenTypes
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.highlighter.HighlighterIterator

class UIQuoteHandler : SimpleTokenSetQuoteHandler(UITokenTypes.STRING) {
    override fun hasNonClosedLiteral(editor: Editor, iterator: HighlighterIterator, offset: Int): Boolean {
        return iterator.tokenType == UITokenTypes.STRING && offset == iterator.start
    }
}
