package app.ultradev.uifiles.psi

import app.ultradev.hytaleuiparser.ast.*
import app.ultradev.uifiles.UIFile
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.psi.*
import com.intellij.openapi.util.TextRange
import javax.swing.Icon

class UiAstNamedPsiElement(
    file: UIFile,
    astNode: AstNode
) : UiAstPsiElement(file, astNode), PsiNamedElement {
    override fun getName(): String {
        return when (astNode) {
            is NodeAssignVariable -> astNode.variable!!.identifier
            is NodeAssignReference -> astNode.variable!!.identifier
            else -> "Unknown node: ${astNode.javaClass.simpleName}"
        }
    }
    
    override fun setName(name: String): PsiElement {
        TODO()
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
