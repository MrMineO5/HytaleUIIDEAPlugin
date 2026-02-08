package app.ultradev.uifiles.annotation

import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.service.UIAnalysisService
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.ProblemHighlightType
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

            when (diag) {
                is UIAnalysisService.UIErrorParse -> {
                    holder
                        .newAnnotation(
                            HighlightSeverity.ERROR,
                            diag.error.message ?: "Parse error"
                        )
                        .range(diag.range)
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .create()
                }

                is UIAnalysisService.UIErrorValidate -> {
                    val annotation = holder.newAnnotation(
                        HighlightSeverity.ERROR,
                        diag.error.message
                    )
                        .range(diag.range)

                    if (diag.error.cause != null) {
                        annotation
                            .tooltip(diag.error.message + "<br/><br/>Caused by: " + diag.error.cause!!.message)
//                            .withFix(IntentionAction)
                    }

                    annotation.create()
                }


                is UIAnalysisService.UIErrorParseRecoverable -> {
                    val annotation = holder.newAnnotation(
                        HighlightSeverity.ERROR,
                        diag.error.message
                    )
                        .range(diag.range)

                    annotation.create()
                }
            }
        }
    }
}
