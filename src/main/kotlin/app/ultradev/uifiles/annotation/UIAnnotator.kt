package app.ultradev.uifiles.annotation

import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.service.UIAnalysisService
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

class UIAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is UIFile) return
        val file = element.virtualFile ?: return
        val project = element.project

        val psiManager = PsiDocumentManager.getInstance(project)
        if (!psiManager.isCommitted(element.containingFile.fileDocument)) return

        val service = project.getService(UIAnalysisService::class.java)
        val diagnostics = service.getDiagnostics(file)

        for (diag in diagnostics) {
            if (!diag.range.intersects(element.textRange)) continue

            holder.newAnnotation(
                HighlightSeverity.ERROR,
                diag.message
            ).range(diag.range).create()
        }
    }
}
