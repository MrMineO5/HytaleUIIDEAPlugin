package app.ultradev.uifiles.documentation

import app.ultradev.hytaleuiparser.ast.*
import app.ultradev.hytaleuiparser.validation.types.displayFullStructure
import app.ultradev.hytaleuiparser.validation.types.displayName
import com.intellij.icons.AllIcons
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.model.Pointer
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation

class UIDocumentationTarget(val node: AstNode) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> = Pointer.hardPointer(this)

    override fun computePresentation(): TargetPresentation {
        return TargetPresentation.builder(node.text)
            .locationText(node.computePath(), AllIcons.Nodes.Field)
            .presentation()
    }

    override fun computeDocumentation(): DocumentationResult {
        return DocumentationResult.documentation(
            buildString {
                when (node) {
                    is NodeIdentifier -> {
                        when (val parent = node.parent) {
                            is NodeField -> {
                                val type = parent.resolvedType
                                append(DocumentationMarkup.DEFINITION_START)
                                append("${node.identifier}: ${type.name}")
                                append(DocumentationMarkup.DEFINITION_END)

                                if (!type.isPrimitive) {
                                    append(DocumentationMarkup.CONTENT_START)
                                    append("<pre><code>")
                                    append(parent.resolvedType.displayFullStructure().replace("\n", "<br/>"))
                                    append("</code></pre>")
                                    append(DocumentationMarkup.CONTENT_END)
                                }
                            }

                            is NodeElement -> {
                                append("<b>${node.identifier}</b>")
                                append("<pre><code>")
                                append(parent.resolvedType.displayFullStructure().replace("\n", "<br/>"))
                                append("</code></pre>")
                            }

                            is NodeType -> {
                                append("<b>${node.identifier}</b>")
                                append("<pre><code>")
                                append(parent.resolvedTypes.displayFullStructure().replace("\n", "<br/>"))
                                append("</code></pre>")
                            }

                            else -> thisLogger().warn("Unknown parent: ${parent.javaClass.simpleName}")
                        }
                    }

                    is NodeVariable -> {
                        val value = node.deepResolve() ?: return@buildString
                        if (value is NodeElement) {
                            val elementType = value.resolvedType
                            append(DocumentationMarkup.DEFINITION_START)
                            append("${node.identifier}: ${elementType.name}")
                            append(DocumentationMarkup.DEFINITION_END)
                        } else {
                            val types = value.resolvedTypes

                            append(DocumentationMarkup.DEFINITION_START)
                            append("${node.identifier}: ${types.displayName()}")
                            append(DocumentationMarkup.DEFINITION_END)

                            append(DocumentationMarkup.CONTENT_START)
                            append("<pre><code>")
                            when (value) {
                                is NodeType -> {
                                    fun resolve(type: NodeType): String {
                                        return "${type.resolvedTypes.displayName()}(\n" + type.resolveValue(true)
                                            .entries
                                            .joinToString(",\n") { (name, value) ->
                                                val value =
                                                    if (value is NodeType) resolve(value) else value.asAstNode.text
                                                "$name: $value"
                                            }.prependIndent() + "\n)"
                                    }

                                    append(resolve(value).replace("\n", "<br/>"))
                                }

                                else -> {
                                    append(value.asAstNode.text)
                                }
                            }
                            append("</code></pre>")
                            append(DocumentationMarkup.CONTENT_END)
                        }
                    }

                    else -> thisLogger().warn("Unknown node: ${node.javaClass.simpleName}")
                }
            }
        )
    }
}