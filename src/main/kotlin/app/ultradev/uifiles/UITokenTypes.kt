package app.ultradev.uifiles

import app.ultradev.hytaleuiparser.token.Token
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType

object UITokenTypes {
    val FILE = IFileElementType("UI_FILE", UILanguage.INSTANCE)
    
    val IDENTIFIER = IElementType("IDENTIFIER", UILanguage.INSTANCE)
    val VARIABLE = IElementType("VARIABLE", UILanguage.INSTANCE)
    val REFERENCE = IElementType("REFERENCE", UILanguage.INSTANCE)
    val ASSIGNMENT = IElementType("ASSIGNMENT", UILanguage.INSTANCE)
    val MEMBER_MARKER = IElementType("MEMBER_MARKER", UILanguage.INSTANCE)
    val END_STATEMENT = IElementType("END_STATEMENT", UILanguage.INSTANCE)
    val SPREAD = IElementType("SPREAD", UILanguage.INSTANCE)
    val START_ELEMENT = IElementType("START_ELEMENT", UILanguage.INSTANCE)
    val END_ELEMENT = IElementType("END_ELEMENT", UILanguage.INSTANCE)
    val STRING = IElementType("STRING", UILanguage.INSTANCE)
    val NUMBER = IElementType("NUMBER", UILanguage.INSTANCE)
    val START_PARENTHESIS = IElementType("START_PARENTHESIS", UILanguage.INSTANCE)
    val END_PARENTHESIS = IElementType("END_PARENTHESIS", UILanguage.INSTANCE)
    val SELECTOR = IElementType("SELECTOR", UILanguage.INSTANCE)
    val FIELD_MARKER = IElementType("FIELD_MARKER", UILanguage.INSTANCE)
    val FIELD_DELIMITER = IElementType("FIELD_DELIMITER", UILanguage.INSTANCE)
    val COMMENT = IElementType("COMMENT", UILanguage.INSTANCE)
    val START_ARRAY = IElementType("START_ARRAY", UILanguage.INSTANCE)
    val END_ARRAY = IElementType("END_ARRAY", UILanguage.INSTANCE)
    val MATH_ADD = IElementType("MATH_ADD", UILanguage.INSTANCE)
    val MATH_SUBTRACT = IElementType("MATH_SUBTRACT", UILanguage.INSTANCE)
    val MATH_MULTIPLY = IElementType("MATH_MULTIPLY", UILanguage.INSTANCE)
    val MATH_DIVIDE = IElementType("MATH_DIVIDE", UILanguage.INSTANCE)
    val TRANSLATION_MARKER = IElementType("TRANSLATION_MARKER", UILanguage.INSTANCE)
    val ERROR = IElementType("ERROR", UILanguage.INSTANCE)
    val WHITESPACE = IElementType("WHITESPACE", UILanguage.INSTANCE)
    val EOF = IElementType("EOF", UILanguage.INSTANCE)

    fun fromTokenType(type: Token.Type): IElementType {
        return when (type) {
            Token.Type.IDENTIFIER -> IDENTIFIER
            Token.Type.VARIABLE -> VARIABLE
            Token.Type.REFERENCE -> REFERENCE
            Token.Type.ASSIGNMENT -> ASSIGNMENT
            Token.Type.MEMBER_MARKER -> MEMBER_MARKER
            Token.Type.END_STATEMENT -> END_STATEMENT
            Token.Type.SPREAD -> SPREAD
            Token.Type.START_ELEMENT -> START_ELEMENT
            Token.Type.END_ELEMENT -> END_ELEMENT
            Token.Type.STRING -> STRING
            Token.Type.NUMBER -> NUMBER
            Token.Type.START_PARENTHESIS -> START_PARENTHESIS
            Token.Type.END_PARENTHESIS -> END_PARENTHESIS
            Token.Type.SELECTOR -> SELECTOR
            Token.Type.FIELD_MARKER -> FIELD_MARKER
            Token.Type.FIELD_DELIMITER -> FIELD_DELIMITER
            Token.Type.COMMENT -> COMMENT
            Token.Type.START_ARRAY -> START_ARRAY
            Token.Type.END_ARRAY -> END_ARRAY
            Token.Type.MATH_ADD -> MATH_ADD
            Token.Type.MATH_SUBTRACT -> MATH_SUBTRACT
            Token.Type.MATH_MULTIPLY -> MATH_MULTIPLY
            Token.Type.MATH_DIVIDE -> MATH_DIVIDE
            Token.Type.TRANSLATION_MARKER -> TRANSLATION_MARKER
            Token.Type.WHITESPACE -> WHITESPACE
            Token.Type.UNKNOWN -> ERROR
            Token.Type.EOF -> EOF
        }
    }
}
