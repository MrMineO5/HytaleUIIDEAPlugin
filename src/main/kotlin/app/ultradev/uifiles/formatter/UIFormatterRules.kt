package app.ultradev.uifiles.formatter

import app.ultradev.hytaleuiparser.ast.*
import app.ultradev.hytaleuiparser.token.Token
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.psi.codeStyle.CodeStyleSettings

object UIFormatterRules {
    fun spacing(
        left: Block?, right: Block, settings: CodeStyleSettings
    ): Spacing? {
        if (left !is UIAstBlock || right !is UIAstBlock) {
            return null
        }

        val common = settings.getCommonSettings("UI")

        // No space before ;
        if (right.node is NodeToken) {
            if (right.node.token.type == Token.Type.END_STATEMENT) {
                return Spacing.createSpacing(
                    0, 0, 0,
                    common.KEEP_LINE_BREAKS, common.KEEP_BLANK_LINES_IN_CODE
                )
            }
        }

        // Space after `:` and `,`
        if ((left.node is NodeToken && left.node.token.type == Token.Type.FIELD_MARKER)
            || (left.node is NodeField && left.node.endStatement?.token?.type == Token.Type.FIELD_DELIMITER)
        ) {
            return Spacing.createSpacing(
                1, 1, 0,
                common.KEEP_LINE_BREAKS, common.KEEP_BLANK_LINES_IN_CODE
            )
        }

        // Spaces around `=`
        if (left.node is NodeToken && (left.node.parent is NodeAssignVariable || left.node.parent is NodeAssignReference)
            || right.node is NodeToken && (right.node.parent is NodeAssignVariable || right.node.parent is NodeAssignReference)) {
            return Spacing.createSpacing(
                1, 1, 0, common.KEEP_LINE_BREAKS, common.KEEP_BLANK_LINES_IN_CODE
            )
        }

        // Space after identifier before `{`
        if (left.node is NodeIdentifier && left.node.parent is NodeElement) {
            return Spacing.createSpacing(
                1, 1, 0, common.KEEP_LINE_BREAKS, common.KEEP_BLANK_LINES_IN_CODE
            )
        }

        return null
    }
}
