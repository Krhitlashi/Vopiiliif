package aih.iikrhia.vopiiliif

import aih.iikrhia.vopiiliif.network.SearchResult
import aih.iikrhia.vopiiliif.network.queryMatchRanges
import aih.iikrhia.vopiiliif.network.rankSearchResults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private fun result(title: String) = SearchResult(title, null)

class SearchRankingTest {

    @Test
    fun exactTitleMatch_ranksFirst() {
        val results = listOf(
            result("Python (programming language)"),
            result("Python"),
            result("Monty Python"),
            result("Pythonidae")
        )
        val ranked = rankSearchResults(results, "python")
        assertEquals("Python", ranked.first().title)
    }

    @Test
    fun prefixMatch_outranksSubstringAndUnrelated() {
        val results = listOf(
            result("A Talk about Jupiter Moons"),
            result("Jupiter"),
            result("Something unrelated")
        )
        val ranked = rankSearchResults(results, "jupiter")
        assertEquals("Jupiter", ranked[0].title)
        assertEquals("A Talk about Jupiter Moons", ranked[1].title)
        assertEquals("Something unrelated", ranked[2].title)
    }

    @Test
    fun resultMatchingMoreWords_ranksAboveFewer() {
        // Both contain "red", but only the second also contains "fox".
        val results = listOf(
            result("Red Panda"),
            result("Red Fox")
        )
        val ranked = rankSearchResults(results, "red fox")
        assertEquals("Red Fox", ranked.first().title)
    }

    @Test
    fun unrelatedResults_keepOriginalOrderAtEnd() {
        val results = listOf(
            result("Aardvark"),
            result("Zebra"),
            result("Money")
        )
        val ranked = rankSearchResults(results, "money")
        assertEquals("Money", ranked.first().title)
        // The two non-matching results retain their server order at the bottom.
        assertEquals(listOf("Aardvark", "Zebra"), ranked.drop(1).map { it.title })
    }

    @Test
    fun blankQuery_returnsOriginalOrder() {
        val results = listOf(result("B"), result("A"))
        assertEquals(listOf("B", "A"), rankSearchResults(results, "  ").map { it.title })
    }

    @Test
    fun matchRanges_findsPhraseAndWords_caseInsensitively() {
        val ranges = queryMatchRanges("Red Fox and red pandas", "red fox")
        // Highlights the "Red Fox" phrase and the standalone "red": "fox" only
        // occurs inside the phrase, so it does not expand the highlight.
        assertEquals(listOf(0 until 7, 12 until 15), ranges)
    }

    @Test
    fun matchRanges_mergesAdjacentAndOverlapping() {
        val ranges = queryMatchRanges("new york new york", "new")
        // Both "new" words: 0..3 and 9..12 (space sits at index 8).
        assertEquals(listOf(0 until 3, 9 until 12), ranges)
        // Overlapping single-word hits merge into one contiguous range. "banana"
        // holds "ban" (0..3) and "ana" twice (1..4 and 3..6), so the trio merges
        // into a single 0..6 block covering the whole word.
        val merged = queryMatchRanges("banana", "ban ana")
        assertEquals(listOf(0 until 6), merged)
        assertTrue(merged.first().first == 0 && merged.first().last == 5)
        // No match yields no ranges.
        assertTrue(queryMatchRanges("nothing relevant", "zebra").isEmpty())
    }
}