package app.ultradev.uifiles.service

import app.ultradev.hytaleuiparser.ParserError
import app.ultradev.hytaleuiparser.ParserException
import app.ultradev.hytaleuiparser.ValidatorError
import app.ultradev.uifiles.ideaTextRange
import com.intellij.openapi.util.TextRange

sealed interface UIError {
    val range: TextRange

    data class UIErrorParseRecoverable(val error: ParserError) : UIError {
        override val range: TextRange get() = error.token.ideaTextRange
    }
    data class UIErrorParse(val error: ParserException) : UIError {
        override val range: TextRange get() = error.token.ideaTextRange
    }
    data class UIErrorValidate(val error: ValidatorError) : UIError {
        override val range: TextRange get() = error.node.ideaTextRange
    }
}