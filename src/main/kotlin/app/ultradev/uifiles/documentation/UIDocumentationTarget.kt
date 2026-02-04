package app.ultradev.uifiles.documentation

import app.ultradev.hytaleuiparser.ast.*
import app.ultradev.hytaleuiparser.validation.types.displayFullStructure
import app.ultradev.hytaleuiparser.validation.types.displayName
import app.ultradev.uifiles.syntax.UISyntaxHighlighter
import app.ultradev.uifiles.UITokenTypes
import com.intellij.icons.AllIcons
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.model.Pointer
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.ui.ColorUtil
import java.awt.Font

class UIDocumentationTarget(val node: AstNode) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> = Pointer.hardPointer(this)

    override fun computePresentation(): TargetPresentation {
        return TargetPresentation.builder(node.text)
            .locationText(node.computePath(), AllIcons.Nodes.Field)
            .presentation()
    }

    override fun computeDocumentation(): DocumentationResult? {
        val htmlBuilder = HtmlBuilder()
        when (node) {
            is NodeIdentifier -> {
                when (val parent = node.parent) {
                    is NodeField -> {
                        val type = parent.resolvedType
                        htmlBuilder.append(HtmlChunk.raw(DocumentationMarkup.DEFINITION_START))
                            .append(getFormattedSpan(node.identifier, DefaultLanguageHighlighterColors.IDENTIFIER))
                            .append(HtmlChunk.text(": "))
                            // Idk, CLASS_NAME is better than something else
                            .append(getFormattedSpan(type.name, DefaultLanguageHighlighterColors.CLASS_NAME))
                            .append(HtmlChunk.raw(DocumentationMarkup.DEFINITION_END))

                        if (!type.isPrimitive) {
                            htmlBuilder.append(HtmlChunk.raw(DocumentationMarkup.CONTENT_START))
                                .append(formatCode(parent.resolvedType.displayFullStructure()))
                                .append(HtmlChunk.raw(DocumentationMarkup.CONTENT_END))
                        }
                    }

                    is NodeElement -> {
                        htmlBuilder.append(HtmlChunk.tag("b").addText(node.identifier))
                        htmlBuilder.append(formatCode(parent.resolvedType.displayFullStructure()))
                    }

                    is NodeType -> {
                        htmlBuilder.append(HtmlChunk.tag("b").addText(node.identifier))
                        htmlBuilder.append(formatCode(parent.resolvedTypes.displayFullStructure()))
                    }

                    else -> thisLogger().warn("Unknown parent: ${parent.javaClass.simpleName}")
                }
            }

            is NodeVariable -> {
                val value = node.deepResolve() ?: return null
                if (value is NodeElement) {
                    val elementType = value.resolvedType
                    htmlBuilder.append(HtmlChunk.raw(DocumentationMarkup.DEFINITION_START))
                        .append(getFormattedSpan(node.identifier, DefaultLanguageHighlighterColors.IDENTIFIER))
                        .append(HtmlChunk.text(": "))
                        .append(getFormattedSpan(elementType.name, DefaultLanguageHighlighterColors.CLASS_NAME))
                        .append(HtmlChunk.raw(DocumentationMarkup.DEFINITION_END))
                } else {
                    val types = value.resolvedTypes

                    htmlBuilder.append(HtmlChunk.raw(DocumentationMarkup.DEFINITION_START))
                        .append(getFormattedSpan(node.identifier, DefaultLanguageHighlighterColors.IDENTIFIER))
                        .append(HtmlChunk.text(": "))
                        .append(
                            getFormattedSpan(
                                types?.displayName() ?: "Unknown",
                                DefaultLanguageHighlighterColors.CLASS_NAME
                            )
                        )
                        .append(HtmlChunk.raw(DocumentationMarkup.DEFINITION_END))

                    htmlBuilder.append(HtmlChunk.raw(DocumentationMarkup.CONTENT_START))
                        .append(
                            HtmlChunk.tag("pre").child(
                                HtmlChunk.tag("code").addRaw(highlightValue(value).toString())
                            )
                        )
                        .append(HtmlChunk.raw(DocumentationMarkup.CONTENT_END))
                }
            }

            else -> thisLogger().warn("Unknown node: ${node.javaClass.simpleName}")
        }

        if (htmlBuilder.isEmpty) return null
        return DocumentationResult.documentation(htmlBuilder.toString())
    }

    private fun highlightValue(value: VariableValue, indent: String = ""): HtmlChunk {
        return when (value) {
            is NodeType -> {
                val builder = HtmlBuilder()
                builder.append(getFormattedSpan(value.resolvedTypes.displayName(), DefaultLanguageHighlighterColors.IDENTIFIER))
                builder.append(HtmlChunk.text("(\n"))
                val newIndent = "$indent    "
                val entries = value.resolveValue(true).entries.toList()
                for ((index, entry) in entries.withIndex()) {
                    builder.append(HtmlChunk.text(newIndent))
                    builder.append(getFormattedSpan(entry.key, DefaultLanguageHighlighterColors.CLASS_NAME))
                    builder.append(HtmlChunk.text(": "))
                    builder.append(highlightValue(entry.value, newIndent))
                    if (index < entries.size - 1) {
                        builder.append(HtmlChunk.text(","))
                    }
                    builder.append(HtmlChunk.br())
                }
                builder.append(HtmlChunk.text("$indent)"))
                HtmlChunk.raw(builder.toString())
            }
            else -> {
                val builder = HtmlBuilder()
                for (token in value.asAstNode.tokens) {
                    val tokenType = UITokenTypes.fromTokenType(token.type)
                    val highlights = UISyntaxHighlighter.getTokenHighlights(tokenType)
                    if (highlights.isNotEmpty()) {
                        builder.append(getFormattedSpan(token.text, highlights[0]))
                    } else {
                        builder.append(HtmlChunk.text(token.text))
                    }
                }
                HtmlChunk.raw(builder.toString())
            }
        }
    }

    private fun formatCode(code: String): HtmlChunk {
        val lines = code.split("\n")
        val builder = HtmlBuilder()
        for ((index, line) in lines.withIndex()) {
            val splitter = line.split(':', limit = 2);
            if (!splitter.isEmpty() && splitter.size == 2) {
                builder.append(getFormattedSpan(splitter[0], DefaultLanguageHighlighterColors.IDENTIFIER));
                builder.append(HtmlChunk.text(":"));
                builder.append(getFormattedSpan(splitter[1], DefaultLanguageHighlighterColors.CLASS_NAME));
            } else {
                builder.append(HtmlChunk.text(line))
            }
            if (index < lines.size - 1) {
                builder.append(HtmlChunk.br())
            }
        }
        return HtmlChunk.tag("pre").child(HtmlChunk.tag("code").addRaw(builder.toString()))
    }
    
    private fun getFormattedSpan(text: String, key: TextAttributesKey): HtmlChunk {
        val scheme = EditorColorsManager.getInstance().globalScheme
        val attributes = scheme.getAttributes(key) ?: return HtmlChunk.text(text)

        var chunk = HtmlChunk.text(text)

        val foreground = attributes.foregroundColor
        if (foreground != null) {
            chunk = HtmlChunk.span("color: #${ColorUtil.toHex(foreground)}").child(chunk)
        }

        if ((attributes.fontType and Font.BOLD) != 0) {
            chunk = HtmlChunk.tag("b").child(chunk)
        }
        if ((attributes.fontType and Font.ITALIC) != 0) {
            chunk = HtmlChunk.tag("i").child(chunk)
        }

        return chunk
    }
}