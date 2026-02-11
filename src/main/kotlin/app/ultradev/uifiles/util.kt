package app.ultradev.uifiles

import app.ultradev.hytaleuiparser.ast.AstNode
import app.ultradev.hytaleuiparser.token.Token
import com.intellij.openapi.util.TextRange

val AstNode.ideaTextRange: TextRange get() = this.textRange.let { TextRange(it.first, it.second) }
val Token.ideaTextRange: TextRange get() = TextRange(this.startOffset, this.startOffset + this.text.length)

fun String.truncate(maxLength: Int): String {
    return if (this.length <= maxLength) this else take(maxLength - 3) + "..."
}
