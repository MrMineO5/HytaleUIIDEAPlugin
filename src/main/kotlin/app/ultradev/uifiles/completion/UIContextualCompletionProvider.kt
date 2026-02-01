package app.ultradev.uifiles.completion

import app.ultradev.hytaleuiparser.ast.*
import app.ultradev.hytaleuiparser.ast.visitor.findNodeAtOffset
import app.ultradev.hytaleuiparser.validation.ElementType
import app.ultradev.hytaleuiparser.validation.Scope
import app.ultradev.hytaleuiparser.validation.types.TypeType
import app.ultradev.hytaleuiparser.validation.types.unifyEnum
import app.ultradev.hytaleuiparser.validation.types.unifyStruct
import app.ultradev.hytaleuiparser.validation.types.unifyStructOrNull
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
        thisLogger().warn("Doing completions uwu")
        val file = parameters.originalFile as? UIFile ?: return
        thisLogger().warn("We're in a UI file! Crazy")
        val rootNode = file.getRootNode() ?: return
        thisLogger().warn("The root node is MINE now")
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
                    completeTypeProperties(parent.resolvedTypes, result)
                }
            }

            nodeAtCursor is NodeConstant -> {
                val parent = nodeAtCursor.parent
                thisLogger().warn("Parent: $parent")
                if (parent is NodeField) {
                    val parentParent = parent.parent.parent
                    thisLogger().warn("Parent parent: $parentParent")
                    if (parentParent is NodeType) {
                        val allowedFields = parentParent.resolvedTypes.unifyStructOrNull() ?: return
                        val fieldType = allowedFields[parent.identifier.identifier]
                        thisLogger().warn("Type: $fieldType")
                        if (fieldType != null) {
                            completeTypeProperties(setOf(fieldType), result)
                        }
                    }
                }
            }

            nodeAtCursor is NodeField -> {
                val parentParent = nodeAtCursor.parent.parent
                if (parentParent is NodeType) {
                    val allowedFields = parentParent.resolvedTypes.unifyStructOrNull() ?: return
                    val fieldType = allowedFields[nodeAtCursor.identifier.identifier]
                    if (fieldType != null) {
                        completeTypeProperties(setOf(fieldType), result)
                    }
                }
            }

            else -> {
                thisLogger().warn("No completion context found for node: ${nodeAtCursor.javaClass.simpleName}")
            }
        }
    }

    private fun findNodeAtOffset(root: RootNode, cursorOffset: Int): AstNode {
        return root.findNodeAtOffset(cursorOffset - 1) ?: root
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
        types: Set<TypeType>, result: CompletionResultSet
    ) {
        if (types.size == 1) {
            val type = types.first()
            if (type.isPrimitive) return
        }

        if (types.all { it.isPrimitive }) return // TODO: Should all be same type but we don't check here

        if (types.all { it.isEnum }) {
            types.unifyEnum().forEach { constant ->
                result.addElement(LookupElementBuilder.create(constant))
            }
            return
        }

        if (types.all { !it.isPrimitive && !it.isEnum }) {
            types.unifyStruct().forEach { (propName, fieldType) ->
                result.addElement(
                    LookupElementBuilder
                        .create(propName)
                        .withTypeText(fieldType.name)
                        .withInsertHandler(UITypePropertyInsertHandler(fieldType, ","))
                )
            }
            return
        }

        thisLogger().warn("Found inconsistent type list: $types")
    }
}
