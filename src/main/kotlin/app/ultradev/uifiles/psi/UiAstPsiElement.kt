package app.ultradev.uifiles.psi

import app.ultradev.hytaleuiparser.ast.AstNode
import app.ultradev.hytaleuiparser.ast.NodeAssignVariable
import app.ultradev.uifiles.UIFile
import app.ultradev.uifiles.ideaTextRange
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.FakePsiElement
import javax.swing.Icon

open class UiAstPsiElement(
    protected val file: UIFile,
    val astNode: AstNode
) : FakePsiElement() {
    override fun getContainingFile(): PsiFile = file
    override fun getParent(): PsiElement = file
    override fun getProject() = file.project
    override fun getTextRange(): TextRange = astNode.ideaTextRange

    override fun navigate(requestFocus: Boolean) {
        val vFile = file.virtualFile ?: return
        OpenFileDescriptor(project, vFile, textRange.startOffset)
            .navigate(requestFocus)
    }

    override fun getName(): String? = "Some text"

    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String {
                // TODO: Ideally we give known node types their own implementation of UiAstPsiElement to avoid these switch statements
                return when (astNode) {
                    is NodeAssignVariable -> "Assign ${astNode.variable!!.identifier}"

                    else -> astNode.javaClass.simpleName
                } + " [${file.name}]"
            }

            override fun getLocationString() = file.name
            override fun getIcon(unused: Boolean): Icon? = null
        }
    }
}
