package app.ultradev.uifiles

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class UIParserDefinition : ParserDefinition {
    override fun createLexer(project: Project): Lexer = UILexer()
    
    override fun createParser(project: Project): PsiParser = PsiParser { root, builder ->
        val marker = builder.mark()

        while (!builder.eof()) {
            val tokenType = builder.tokenType
            if (tokenType != null) {
                builder.advanceLexer()
            } else {
                break
            }
        }

        marker.done(UITokenTypes.FILE)
        builder.treeBuilt
    }
    
    override fun getFileNodeType(): IFileElementType = UITokenTypes.FILE
    
    override fun getCommentTokens(): TokenSet = TokenSet.create(UITokenTypes.COMMENT)
    
    override fun getStringLiteralElements(): TokenSet = TokenSet.create(UITokenTypes.STRING)
    
    override fun getWhitespaceTokens(): TokenSet = TokenSet.create(UITokenTypes.WHITESPACE)
    
    override fun createFile(viewProvider: FileViewProvider): PsiFile = UIFile(viewProvider)
    
    override fun createElement(node: ASTNode): PsiElement = UIPsiElement(node)
}
