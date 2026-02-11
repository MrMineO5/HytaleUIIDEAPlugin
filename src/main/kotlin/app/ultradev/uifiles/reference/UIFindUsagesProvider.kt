package app.ultradev.uifiles.reference

import app.ultradev.uifiles.UILexer
import app.ultradev.uifiles.UITokenSets
import app.ultradev.uifiles.UITokenTypes
import app.ultradev.uifiles.psi.UiAstNamedPsiElement
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

class UIFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner =
        DefaultWordsScanner(
            UILexer(),
            UITokenSets.IDENTIFIERS,
            UITokenSets.COMMENTS,
            UITokenSets.VALUES
        )

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is UiAstNamedPsiElement
    }

    override fun getHelpId(psiElement: PsiElement): @NonNls String {
        return "test"
    }

    override fun getType(element: PsiElement): @Nls String {
        return "test2"
    }

    override fun getDescriptiveName(element: PsiElement): @Nls String {
        return "test3"
    }

    override fun getNodeText(
        element: PsiElement,
        useFullName: Boolean
    ): @Nls String {
        return "test4"
    }
}