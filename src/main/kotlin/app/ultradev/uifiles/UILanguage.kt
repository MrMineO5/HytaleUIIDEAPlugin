package app.ultradev.uifiles

import com.intellij.lang.Language

class UILanguage : Language("UI") {
    companion object {
        val INSTANCE = UILanguage()
    }
}
