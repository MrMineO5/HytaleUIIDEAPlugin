package app.ultradev.uifiles.reference

import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.UIPsiElement
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

class UIReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(UIFile::class.java),
            UIFileReferenceProvider()
        )
    }
}
