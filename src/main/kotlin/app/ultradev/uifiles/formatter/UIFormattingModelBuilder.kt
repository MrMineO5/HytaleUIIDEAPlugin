package app.ultradev.uifiles.formatter

import app.ultradev.uifiles.UIFile
import com.intellij.formatting.*

class UIFormattingModelBuilder : FormattingModelBuilder {

    override fun createModel(context: FormattingContext): FormattingModel {
        val file = context.psiElement.containingFile as? UIFile ?: return FormattingModelProvider.createFormattingModelForPsiFile(
            context.psiElement.containingFile,
            UIFallbackRootBlock(context.psiElement.containingFile, context.codeStyleSettings),
            context.codeStyleSettings
        )
        
        val root = file.getUnvalidatedRootNode() ?: return FormattingModelProvider.createFormattingModelForPsiFile(
            file,
            UIFallbackRootBlock(context.psiElement.containingFile, context.codeStyleSettings),
            context.codeStyleSettings
        )

        return FormattingModelProvider.createFormattingModelForPsiFile(
            file,
            UIRootBlock(root, context.codeStyleSettings),
            context.codeStyleSettings
        )
    }
}
