package app.ultradev.uifiles.completion

import app.ultradev.hytaleuiparser.ast.*
import app.ultradev.hytaleuiparser.ast.visitor.findNodeAtOffset
import app.ultradev.hytaleuiparser.validation.ElementType
import app.ultradev.hytaleuiparser.validation.Scope
import app.ultradev.hytaleuiparser.validation.types.TypeType
import app.ultradev.hytaleuiparser.validation.types.displayName
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
    private fun isElementExpected(variable: VariableReference): Boolean {
        val parent = variable.asAstNode.parent
        return when (parent) {
            is NodeElement -> true
            is NodeRefMember -> isElementExpected(parent)
            else -> false
        }
    }

    private fun determineDesiredType(variable: VariableReference): TypeType? {
        val parent = variable.asAstNode.parent
        return when (parent) {
            is NodeField -> parent.resolvedType
            is VariableReference -> determineDesiredType(parent)
            else -> null
        }
    }

    override fun addCompletions(
        parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet
    ) {
        thisLogger().debug("Running completion provider for ${parameters.originalFile.name} at ${parameters.offset}")
        val file = parameters.originalFile as? UIFile ?: return
        thisLogger().debug("UI file resolved")
        val rootNode = file.getRootNode() ?: return
        val offset = parameters.offset

        var nodeAtCursor = findNodeAtOffset(rootNode, offset)

        if (nodeAtCursor is NodeToken) {
            nodeAtCursor = nodeAtCursor.parent
        }

        thisLogger().debug("Node at cursor: ${nodeAtCursor.javaClass.simpleName} ${nodeAtCursor.text}")

        when {
            nodeAtCursor is RootNode -> {
                completeElementTypes(result)
            }

            nodeAtCursor is NodeVariable -> {
                if (isElementExpected(nodeAtCursor)) {
                    completeElements(nodeAtCursor.resolvedScope!!, result)
                } else {
                    val desiredType = determineDesiredType(nodeAtCursor)
                    thisLogger().debug("Desired type: $desiredType")
                    nodeAtCursor.resolvedScope?.let { completeVariables(it, result, desiredType) }
                }
            }

            nodeAtCursor is NodeReference -> {
                completeReferences(nodeAtCursor.file, result)
            }

            nodeAtCursor is NodeBody -> {
                if (offset - 1 == nodeAtCursor.endToken.token.startOffset) return

                val parent = nodeAtCursor.parent
                if (parent is NodeElement) {
                    completeElementProperties(parent, result)
                    completeElementTypes(result)
                } else if (parent is NodeType) {
                    completeTypeProperties(parent.resolvedTypes, result)
                }
            }

            nodeAtCursor is NodeConstant -> {
                val parent = nodeAtCursor.parent
                if (parent is NodeField) {
                    val parentParent = parent.parent.parent
                    if (parentParent is NodeType) {
                        val allowedFields = parentParent.resolvedTypes.unifyStructOrNull() ?: return
                        val fieldType = allowedFields[parent.identifier.identifier]
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

            nodeAtCursor is NodeIdentifier -> {
                val parent = nodeAtCursor.parent
                if (parent is NodeMemberField) {
                    parent.resolvedTypes?.unifyStructOrNull()?.forEach { (fieldName, fieldType) ->
                        result.addElement(
                            LookupElementBuilder
                                .create(fieldName)
                                .withTypeText(fieldType.name))

                    }
                }

//                completeElementTypes(result)
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
        scope: Scope, result: CompletionResultSet, desiredType: TypeType?
    ) {
        val variables = scope.variableKeys()
            .filter {
                val variable = scope.lookupVariableAssignment(it)?.valueAsVariable ?: return@filter true
                return@filter variable !is NodeElement
            }
        variables.forEach { varName ->
            val variable = scope.lookupVariableAssignment(varName)!!.valueAsVariable
            val resT = variable.resolvedTypes
            result.addElement(
                LookupElementBuilder
                    .create(varName.removePrefix("@"))
                    .withPresentableText(varName)
                    .let {
                        if (resT != null) {
                            it.withBoldness(desiredType != null && desiredType in resT)
                        } else {
                            it.strikeout()
                        }
                    }
                    .withTypeText(variable.resolvedTypes?.displayName() ?: "Unknown")
            )
        }
    }

    private fun completeElements(
        scope: Scope, result: CompletionResultSet
    ) {
        val variables = scope.variableKeys()
            .filter {
                val variable = scope.lookupVariableAssignment(it)?.valueAsVariable ?: return@filter true
                return@filter variable is NodeElement
            }
        variables.forEach { varName ->
            val variable = scope.lookupVariableAssignment(varName)!!.valueAsVariable as? NodeElement
            result.addElement(
                LookupElementBuilder
                    .create(varName.removePrefix("@"))
                    .withPresentableText(varName)
                    .withTypeText(variable?.resolvedType?.name ?: "Unknown")
            )
        }
    }

    private fun completeReferences(
        file: RootNode, result: CompletionResultSet
    ) {
        file.referenceMap.keys.forEach { refName ->
            result.addElement(
                LookupElementBuilder
                    .create(refName)
                    .withPresentableText(refName)
                    .withTypeText("Import")
            )
        }
    }

    private fun completeElementProperties(
        element: NodeElement, result: CompletionResultSet
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
