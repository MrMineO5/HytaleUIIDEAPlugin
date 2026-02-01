package app.ultradev.uifiles.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns

class UICompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(app.ultradev.uifiles.UILanguage.INSTANCE),
            UIContextualCompletionProvider()
        )
    }
}
