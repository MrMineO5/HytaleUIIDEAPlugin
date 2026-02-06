package app.ultradev.uifiles.psi

import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement

class TestElementDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): @NlsSafe String? {
        if (element !is UiAstPsiElement) return null
        // Allow the element to decide its own presentation
        // see SingleTargetElementInfo#doGenerateInfo
        return ""
    }
}