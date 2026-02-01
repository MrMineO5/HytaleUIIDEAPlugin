package app.ultradev.uifiles.completion

import app.ultradev.hytaleuiparser.ast.AstNode
import app.ultradev.hytaleuiparser.ast.visitor.AstVisitor

class FindNodeAtOffsetVisitor(val offset: Int) : AstVisitor {
    var bestMatch: AstNode? = null
    private var bestMatchSize = Int.MAX_VALUE

    override fun visit(node: AstNode) {
        val (start, end) = node.textRange
        if (offset in (start + 1)..end) {
            val size = end - start
            if (size <= bestMatchSize) {
                bestMatch = node
                bestMatchSize = size
            }
        }
    }
}