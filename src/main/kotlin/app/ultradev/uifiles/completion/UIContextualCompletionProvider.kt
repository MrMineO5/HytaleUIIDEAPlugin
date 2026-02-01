package app.ultradev.uifiles.completion

import app.ultradev.hytaleuiparser.ast.*
import app.ultradev.hytaleuiparser.ast.visitor.AstVisitor
import app.ultradev.hytaleuiparser.validation.ElementType
import app.ultradev.hytaleuiparser.validation.Scope
import app.ultradev.hytaleuiparser.validation.types.TypeType
import app.ultradev.uifiles.UIFile
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.ProcessingContext

class UIContextualCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet
    ) {
        val file = parameters.originalFile as? UIFile ?: return
        val rootNode = file.getRootNode() ?: return
        val offset = parameters.offset

        var nodeAtCursor = findNodeAtOffset(rootNode, offset)

        if (nodeAtCursor is NodeToken) {
            nodeAtCursor = nodeAtCursor.parent
        }

        thisLogger().warn("Cursor ${offset}, best match: $nodeAtCursor")

        val text = file.text
        val beforeCursor = text.substring(0, offset)

        thisLogger().warn(beforeCursor)
        thisLogger().warn("${nodeAtCursor.textRange}: ${nodeAtCursor.text}")

        when {
            nodeAtCursor is RootNode -> {
                completeElementTypes(result)
            }

            nodeAtCursor is NodeVariable -> {
                completeVariables(nodeAtCursor.resolvedScope, result)
            }

            nodeAtCursor is NodeReference -> {
                completeReferences(nodeAtCursor.resolvedScope, result)
            }

            nodeAtCursor is NodeBody -> {
                if (offset - 1 == nodeAtCursor.endToken.token.startOffset) return

                val parent = nodeAtCursor.parent
                if (parent is NodeElement) {


                    completeElementProperties(parent, parent.resolvedScope, result)
                    completeElementTypes(result)
                } else if (parent is NodeType) {
                    completeTypeProperties(parent.resolvedType, result)
                }
            }

            nodeAtCursor is NodeConstant -> {
                val parent = nodeAtCursor.parent
                thisLogger().warn("Parent: $parent")
                if (parent is NodeField) {
                    val parentParent = parent.parent.parent
                    thisLogger().warn("Parent parent: $parentParent")
                    if (parentParent is NodeType) {
                        val fieldType = parentParent.resolvedType.allowedFields[parent.identifier.identifier]
                        thisLogger().warn("Type: $fieldType")
                        if (fieldType != null) {
                            completeTypeProperties(fieldType, result)
                        }
                    }
                }
            }

            nodeAtCursor is NodeField -> {
                val parentParent = nodeAtCursor.parent.parent
                if (parentParent is NodeType) {
                    val fieldType = parentParent.resolvedType.allowedFields[nodeAtCursor.identifier.identifier]
                    if (fieldType != null) {
                        completeTypeProperties(fieldType, result)
                    }
                }
            }

            else -> {
                thisLogger().warn("No completion context found for node: ${nodeAtCursor.javaClass.simpleName}")
            }
        }
    }

    private fun findNodeAtOffset(root: RootNode, offset: Int): AstNode {
        val visitor = FindNodeAtOffsetVisitor(offset)
        root.walk(visitor)
        return visitor.bestMatch ?: root
    }


    private fun completeElementTypes(result: CompletionResultSet) {
        ElementType.entries.forEach { elementType ->
            result.addElement(
                LookupElementBuilder
                    .create(elementType.name)
                    .withTypeText("Element")
                    .withIcon(null)
                    .withInsertHandler(UIElementInsertHandler)
            )
        }
    }

    private fun completeVariables(
        scope: Scope, result: CompletionResultSet
    ) {
        scope.variableKeys().forEach { varName ->
            result.addElement(
                LookupElementBuilder
                    .create(varName.removePrefix("@"))
                    .withPresentableText(varName)
                    .withTypeText("Variable")
            )
        }
    }

    private fun completeReferences(
        scope: Scope, result: CompletionResultSet
    ) {
        scope.referenceKeys().forEach { refName ->
            result.addElement(
                LookupElementBuilder
                    .create(refName)
                    .withPresentableText(refName)
                    .withTypeText("Import")
            )
        }
    }

    private fun completeElementProperties(
        element: NodeElement, scope: Scope, result: CompletionResultSet
    ) {
        val allowedProperties = element.resolvedType.properties
        allowedProperties.forEach { (propName, type) ->
            result.addElement(
                LookupElementBuilder
                    .create(propName)
                    .withTypeText(type.name)
                    .withInsertHandler(UITypePropertyInsertHandler(type, ";"))
            )
        }
    }

    private fun completeTypeProperties(
        type: TypeType, result: CompletionResultSet
    ) {
        if (type.isPrimitive) return

        if (type.isEnum) {
            type.enum.forEach { propName ->
                result.addElement(LookupElementBuilder.create(propName))
            }
            return
        }

        val allowedProperties = type.allowedFields
        allowedProperties.forEach { (propName, fieldType) ->
            result.addElement(
                LookupElementBuilder
                    .create(propName)
                    .withTypeText(fieldType.name)
                    .withInsertHandler(UITypePropertyInsertHandler(fieldType, ","))
            )
        }
    }
}
