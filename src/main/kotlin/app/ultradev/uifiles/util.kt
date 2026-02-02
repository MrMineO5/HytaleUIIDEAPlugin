package app.ultradev.uifiles

import app.ultradev.hytaleuiparser.ast.AstNode
import app.ultradev.hytaleuiparser.token.Token
import com.intellij.openapi.util.TextRange

val AstNode.ideaTextRange: TextRange get() = this.textRange.let { TextRange(it.first, it.second) }
val Token.ideaTextRange: TextRange get() = TextRange(this.startOffset, this.startOffset + this.text.length)
