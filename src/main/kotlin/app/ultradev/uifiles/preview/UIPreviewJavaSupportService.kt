package app.ultradev.uifiles.preview

import com.intellij.psi.PsiFile
import org.jetbrains.uast.UFile
import org.jetbrains.uast.toUElement

class UIPreviewJavaSupportService {
    fun getClassNames(file: PsiFile): List<String> {
        val uFile = file.toUElement(UFile::class.java) ?: return emptyList()
        return uFile.classes.mapNotNull { it.javaPsi.qualifiedName }
    }
}
