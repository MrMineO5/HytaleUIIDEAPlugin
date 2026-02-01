package app.ultradev.uifiles.syntax

import app.ultradev.uifiles.UITokenTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

object UISyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = app.ultradev.uifiles.UILexer()

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        return when (tokenType) {
            UITokenTypes.IDENTIFIER -> IDENTIFIER_KEYS
            UITokenTypes.STRING -> STRING_KEYS
            UITokenTypes.NUMBER -> NUMBER_KEYS
            UITokenTypes.COMMENT -> COMMENT_KEYS
            UITokenTypes.VARIABLE, UITokenTypes.REFERENCE -> KEYWORD_KEYS
            UITokenTypes.ASSIGNMENT, UITokenTypes.FIELD_MARKER, UITokenTypes.MEMBER_MARKER -> OPERATOR_KEYS
            UITokenTypes.SELECTOR -> SELECTOR_KEYS
            UITokenTypes.TRANSLATION_MARKER -> TRANSLATION_KEYS
            UITokenTypes.MATH_ADD, UITokenTypes.MATH_SUBTRACT, UITokenTypes.MATH_MULTIPLY, UITokenTypes.MATH_DIVIDE -> OPERATOR_KEYS
            UITokenTypes.ERROR -> ERROR_KEYS
            else -> EMPTY_KEYS
        }
    }

    private val IDENTIFIER_KEYS = arrayOf(DefaultLanguageHighlighterColors.IDENTIFIER)
    private val STRING_KEYS = arrayOf(DefaultLanguageHighlighterColors.STRING)
    private val NUMBER_KEYS = arrayOf(DefaultLanguageHighlighterColors.NUMBER)
    private val COMMENT_KEYS = arrayOf(DefaultLanguageHighlighterColors.LINE_COMMENT)
    private val KEYWORD_KEYS = arrayOf(DefaultLanguageHighlighterColors.KEYWORD)
    private val OPERATOR_KEYS = arrayOf(DefaultLanguageHighlighterColors.OPERATION_SIGN)
    private val SELECTOR_KEYS =
        arrayOf(TextAttributesKey.createTextAttributesKey("UI.SELECTOR", DefaultLanguageHighlighterColors.CONSTANT))
    private val TRANSLATION_KEYS =
        arrayOf(TextAttributesKey.createTextAttributesKey("UI.TRANSLATION", DefaultLanguageHighlighterColors.STRING))
    private val ERROR_KEYS = arrayOf(DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
    private val EMPTY_KEYS = arrayOf<TextAttributesKey>()
}
