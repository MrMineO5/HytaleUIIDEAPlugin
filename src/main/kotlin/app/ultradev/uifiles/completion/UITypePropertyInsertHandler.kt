package app.ultradev.uifiles.completion

import app.ultradev.hytaleuiparser.validation.types.TypeType
import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiDocumentManager

class UITypePropertyInsertHandler(val type: TypeType, val ending: String) : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val document = context.document
        val editor = context.editor

        val startOffset = context.startOffset
        val tailOffset = context.tailOffset

        val defaultValue = type.default()
        val key = item.lookupString

        val text = "$key: $defaultValue$ending"
        document.replaceString(startOffset, tailOffset, text)

        val defaultStart = startOffset + "$key: ".length
        val defaultEnd = defaultStart + defaultValue.length

        editor.caretModel.moveToOffset(defaultEnd)
        editor.selectionModel.setSelection(defaultStart, defaultEnd)

        context.setAddCompletionChar(false)
    }
}