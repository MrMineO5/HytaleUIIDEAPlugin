package app.ultradev.uifiles

import app.ultradev.hytaleuiparser.Tokenizer
import app.ultradev.hytaleuiparser.token.Token
import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType
import com.intellij.util.text.CharSequenceReader
import java.io.StringReader

class UILexer : LexerBase() {

    private var tokenizer: Tokenizer? = null
    private var currentToken: Token? = null

    private var buffer: CharSequence = ""
    private var bufferEnd: Int = 0
    private var baseOffset: Int = 0

    override fun start(
        buffer: CharSequence,
        startOffset: Int,
        endOffset: Int,
        initialState: Int
    ) {
        this.buffer = buffer
        this.bufferEnd = endOffset
        this.baseOffset = startOffset

        tokenizer = Tokenizer(
            CharSequenceReader(buffer.subSequence(startOffset, endOffset))
        )

        advance()
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? {
        return currentToken?.let { UITokenTypes.fromTokenType(it.type) }
    }

    override fun getTokenStart(): Int {
        val token = currentToken ?: return baseOffset
        return baseOffset + token.startOffset
    }

    override fun getTokenEnd(): Int {
        val token = currentToken ?: return baseOffset
        return baseOffset + token.startOffset + token.text.length
    }

    override fun advance() {
        currentToken =
            if (tokenizer?.hasNext() == true) tokenizer?.next()
            else null
    }

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = bufferEnd
}
