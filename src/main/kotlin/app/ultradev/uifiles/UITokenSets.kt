package app.ultradev.uifiles

import com.intellij.psi.tree.TokenSet

object UITokenSets {
    val IDENTIFIERS = TokenSet.create(
        UITokenTypes.IDENTIFIER,
        UITokenTypes.VARIABLE,
        UITokenTypes.REFERENCE,
    )

    val COMMENTS = TokenSet.create(UITokenTypes.COMMENT)

    val VALUES = TokenSet.create(UITokenTypes.STRING, UITokenTypes.NUMBER)
}