package app.ultradev.uifiles

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class UIFileType : LanguageFileType(UILanguage.INSTANCE) {
    override fun getName(): String = "UI File"
    override fun getDescription(): String = "Hytale UI file"
    override fun getDefaultExtension(): String = "ui"
    override fun getIcon(): Icon? = null

    companion object {
        val INSTANCE = UIFileType()
    }
}
