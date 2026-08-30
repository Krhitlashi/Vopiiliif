package aih.iikrhia.vopiiliif

import aih.iikrhia.vopiiliif.network.WikiBlock
import aih.iikrhia.vopiiliif.network.parseHtmlToBlocks
import aih.iikrhia.vopiiliif.network.parseTable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.jsoup.Jsoup

class TableParsingTest {

    // A colspan cell must not push later cells out of the grid: the span emits
    // its content once and blank placeholders for the rest of the span, so the
    // row is exactly as wide as the widest row beneath it.
    @Test
    fun colspanHeader_expandsIntoPlaceholders() {
        val html = """
            <table>
              <tr><th colspan="2">Span</th></tr>
              <tr><td>A</td><td>B</td></tr>
              <tr><td>C</td><td>D</td></tr>
            </table>
        """.trimIndent()
        val table = parseTable(Jsoup.parseBodyFragment(html).body().selectFirst("table")!!, "https://en.wikipedia.org")

        assertEquals(listOf("Span", ""), table.headers)
        assertEquals(listOf(listOf("A", "B"), listOf("C", "D")), table.rows)
        assertTrue(table.cellImages.isEmpty())
    }

    // A rowspan cell occupies the same column in the following rows as an empty
    // placeholder, so the row below starts one column further right instead of
    // sliding left into the phantom gap.
    @Test
    fun rowspan_reservesEmptyPlaceholdersInFollowingRows() {
        val html = """
            <table>
              <tr><th>H1</th><th>H2</th></tr>
              <tr><td rowspan="2">X</td><td>Y</td></tr>
              <tr><td>Z</td></tr>
            </table>
        """.trimIndent()
        val table = parseTable(Jsoup.parseBodyFragment(html).body().selectFirst("table")!!, "https://en.wikipedia.org")

        assertEquals(listOf("H1", "H2"), table.headers)
        assertEquals(listOf(listOf("X", "Y"), listOf("", "Z")), table.rows)
    }

    // Ragged source rows (fewer cells than the widest row) are padded so the
    // renderer can rely on a rectangular grid.
    @Test
    fun raggedRows_arePaddedToRectangularGrid() {
        val html = """
            <table>
              <tr><td>A</td><td>B</td><td>C</td></tr>
              <tr><td>D</td></tr>
            </table>
        """.trimIndent()
        val table = parseTable(Jsoup.parseBodyFragment(html).body().selectFirst("table")!!, "https://en.wikipedia.org")

        assertEquals(listOf("Col 1", "Col 2", "Col 3"), table.headers)
        assertEquals(listOf(listOf("A", "B", "C"), listOf("D", "", "")), table.rows)
    }

    // Images inside a cell are pulled out of the sanitized HTML and recorded in
    // cellImages at the cell's grid position, so the table view can render them
    // in place instead of silently dropping them.
    @Test
    fun cellImages_areExtractedAndRemovedFromHtml() {
        val html = """
            <table>
              <tr><th>Flag</th></tr>
              <tr><td>Vexilloid <img src="//upload.wikimedia.org/wikipedia/commons/thumb/1/15/Cat_August_2010-4.jpg/250px-Cat_August_2010-4.jpg" width="181" height="111"></td></tr>
            </table>
        """.trimIndent()
        val table = parseTable(Jsoup.parseBodyFragment(html).body().selectFirst("table")!!, "https://en.wikipedia.org")

        assertEquals(listOf("Flag"), table.headers)
        assertEquals(listOf(listOf("Vexilloid")), table.rows)
        assertTrue("expected no <img> left in cell html, got ${table.rows[0][0]}", !table.rows[0][0].contains("<img"))
        val images = table.cellImages[0]?.get(0).orEmpty()
        assertEquals(1, images.size)
        assertTrue("expected wikimedia url, got ${images[0].url}", images[0].url.contains("Cat_August_2010-4.jpg"))
    }

    // parseHtmlToBlocks must not emit table-cell images twice: they belong to
    // the Table block now (via cellImages), not as detached image blocks.
    @Test
    fun parseHtmlToBlocks_keepsCellImagesInsideTable() {
        val html = """
            <div class="mw-parser-output">
              <p>Intro</p>
              <table>
                <tr><th>Name</th><th>Flag</th></tr>
                <tr><td>Cat</td><td><img src="//upload.wikimedia.org/wikipedia/commons/thumb/1/15/Cat_August_2010-4.jpg/250px-Cat_August_2010-4.jpg" width="181" height="111"></td></tr>
              </table>
            </div>
        """.trimIndent()
        val blocks = parseHtmlToBlocks(html, "https://en.wikipedia.org")

        val tables = blocks.filterIsInstance<WikiBlock.Table>()
        assertEquals("expected exactly one table block", 1, tables.size)
        assertEquals(1, tables[0].cellImages[0]?.get(1).orEmpty().size)
        assertEquals("expected no detached image blocks for table cells, got ${blocks.filterIsInstance<WikiBlock.Image>()}", 0, blocks.count { it is WikiBlock.Image })
    }

    // A table nested inside a cell must not leak its rows into the outer grid.
    @Test
    fun nestedTable_doesNotLeakRows() {
        val html = """
            <table>
              <tr><th>Outer</th></tr>
              <tr><td>inner <table><tr><td>a</td><td>b</td></tr><tr><td>c</td><td>d</td></tr></table></td></tr>
            </table>
        """.trimIndent()
        val table = parseTable(Jsoup.parseBodyFragment(html).body().selectFirst("table")!!, "https://en.wikipedia.org")

        assertEquals(listOf("Outer"), table.headers)
        assertEquals(1, table.rows.size)
        assertEquals(1, table.rows[0].size)
        assertTrue(table.rows[0][0].contains("a"))
        assertTrue(table.rows[0][0].contains("d"))
    }
}
