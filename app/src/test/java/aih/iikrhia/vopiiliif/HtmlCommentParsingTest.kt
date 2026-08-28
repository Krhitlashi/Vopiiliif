package aih.iikrhia.vopiiliif

import aih.iikrhia.vopiiliif.network.parseHtmlToBlocks
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlCommentParsingTest {

    // Regression: sanitizeElementInline used to call Iterator.remove() on Jsoup's
    // unmodifiable childNodes() view, throwing UnsupportedOperationException (null
    // message) on any page containing an HTML comment - surfaced to the user as
    // "Failed to fetch article details - null"/"unknown error".
    @Test
    fun commentsInsideBlocks_parseWithoutThrowing() {
        val html = """
            <div class="mw-parser-output">
              <p>First paragraph.<!-- COMMENT_LEAK_MARKER --></p>
              <!-- COMMENT_LEAK_MARKER between paragraphs -->
              <p>Second <b>bold</b> paragraph</p>
              <ul><li>Item <!-- COMMENT_LEAK_MARKER --> one</li></ul>
              <h2><span class="mw-headline" id="History">History</span><!-- COMMENT_LEAK_MARKER --></h2>
              <table><tr><th>H1</th><th>H2</th></tr><tr><td>A</td><td>B</td></tr></table>
            </div>
        """.trimIndent()

        val blocks = parseHtmlToBlocks(html, "https://en.wikipedia.org")

        assertTrue("expected at least some blocks, got ${blocks.size}", blocks.isNotEmpty())
        val rendered = blocks.joinToString("\n") { it.toString() }
        assertTrue("comment marker leaked into rendered content: $rendered", !rendered.contains("COMMENT_LEAK_MARKER"))
        assertTrue("expected lead paragraph text, got: $rendered", rendered.contains("First paragraph."))
    }
}
