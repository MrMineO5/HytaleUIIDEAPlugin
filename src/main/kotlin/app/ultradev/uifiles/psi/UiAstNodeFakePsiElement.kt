package app.ultradev.uifiles.psi

import app.ultradev.hytaleuiparser.ast.*
import app.ultradev.uifiles.UIFile
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.*
import com.intellij.psi.impl.FakePsiElement
import com.intellij.openapi.util.TextRange
import javax.swing.Icon

class UiAstNodeFakePsiElement(
    private val file: UIFile,
    val astNode: AstNode
) : FakePsiElement(), PsiNamedElement {
    override fun getName(): String {
        return when (astNode) {
            is NodeAssignVariable -> astNode.variable.identifier
            is NodeAssignReference -> astNode.variable.identifier
            else -> "Unknown node: ${astNode.javaClass.simpleName}"
        }
    }
    
    override fun setName(name: String): PsiElement {
        TODO()
    }
    
    override fun getTextRange(): TextRange {
        return astNode.textRange.let { TextRange(it.first, it.second) }
    }

    override fun getContainingFile(): PsiFile {
        return file
    }

    override fun getParent(): PsiElement {
        return file
    }

    override fun navigate(requestFocus: Boolean) {
        val vFile = file.virtualFile ?: return
        OpenFileDescriptor(project, vFile, textRange.startOffset)
            .navigate(requestFocus)
    }

    override fun getPresentableText(): String = "$name [${file.name}]"

    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText() = name
            override fun getLocationString() = file.name
            override fun getIcon(unused: Boolean): Icon? = null
        }
    }
}
