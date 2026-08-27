package aih.iikrhia.vopiiliif

import aih.iikrhia.vopiiliif.network.WikiBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SectionGroupingTest {

    @Test
    fun articleWithLeadParagraph_introIsFirstSection() {
        val blocks = listOf(
            WikiBlock.Paragraph("Lead paragraph."),
            WikiBlock.Heading("History", 2),
            WikiBlock.Paragraph("History text."),
            WikiBlock.Heading("References", 2),
            WikiBlock.Paragraph("Refs.")
        )
        val sections = groupBlocksIntoSections(blocks, "Introduction")
        assertEquals("Introduction", sections.first().title)
        assertEquals(listOf("History", "References"), sections.drop(1).map { it.title })
    }

    @Test
    fun articleStartingWithHeading_doesNotDuplicateIntroInRemaining() {
        // No lead paragraph: the intro fallback never flushes, so a later section whose
        // title happens to equal the intro title becomes introSection. ExtractDetail must
        // exclude it from the section list or the LazyColumn gets a duplicate item key
        // and crashes with "Key was already used".
        val blocks = listOf(
            WikiBlock.Heading("History", 2),
            WikiBlock.Paragraph("History text."),
            WikiBlock.Heading("Introduction", 2),
            WikiBlock.Paragraph("Intro text."),
            WikiBlock.Heading("References", 2),
            WikiBlock.Paragraph("Refs.")
        )
        val sections = groupBlocksIntoSections(blocks, "Introduction")
        val introTitle = "Introduction"
        val introSection = sections.firstOrNull { it.title == introTitle }
        assertEquals("Introduction", introSection?.title)
        // Mirror ExtractDetail's computation ( filterNot excludes the intro by id ).
        val remaining = if (introSection != null) sections.filterNot { it.id == introSection.id } else sections
        assertNull("intro section must not remain in the list", remaining.firstOrNull { it.id == introSection?.id })
        assertEquals(listOf("History", "References"), remaining.map { it.title })
        val ids = (listOfNotNull(introSection?.id) + remaining.map { it.id })
        assertEquals("ids must be unique", ids.size, ids.toSet().size)
    }

    @Test
    fun idsAreUniqueAcrossAllSections() {
        val blocks = listOf(
            WikiBlock.Heading("One", 2),
            WikiBlock.Heading("Sub A", 3),
            WikiBlock.Paragraph("x"),
            WikiBlock.Heading("Sub B", 4),
            WikiBlock.Paragraph("y"),
            WikiBlock.Heading("Two", 2),
            WikiBlock.Heading("Three", 2)
        )
        val sections = groupBlocksIntoSections(blocks, "Introduction")
        fun collectIds(sectionList: List<ArticleSection>): List<String> =
            sectionList.flatMap { listOf(it.id) + collectIds(it.subSections) }
        val ids = collectIds(sections)
        assertEquals("ids must be unique", ids.size, ids.toSet().size)
    }
}
