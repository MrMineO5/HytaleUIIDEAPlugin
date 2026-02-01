package app.ultradev.uifiles

import app.ultradev.hytaleuiparser.ast.AstNode
import com.intellij.openapi.util.TextRange

val AstNode.ideaTextRange: TextRange get() = this.textRange.let { TextRange(it.first, it.second) }