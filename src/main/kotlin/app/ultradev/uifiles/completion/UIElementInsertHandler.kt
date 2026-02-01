package app.ultradev.uifiles.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement

object UIElementInsertHandler : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val document = context.document
        val editor = context.editor

        val startOffset = context.startOffset
        val tailOffset = context.tailOffset

        document.replaceString(
            startOffset,
            tailOffset,
            "${item.lookupString} {}"
        )

        val caretOffset = startOffset + "${item.lookupString} {".length
        editor.caretModel.moveToOffset(caretOffset)

        context.setAddCompletionChar(false)
    }
}