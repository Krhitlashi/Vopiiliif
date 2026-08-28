package aih.iikrhia.vopiiliif.network

import com.squareup.moshi.JsonClass
import kotlin.text.RegexOption
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

@JsonClass(generateAdapter = true)
data class SearchResponse(
    val query: SearchQuery?
)

@JsonClass(generateAdapter = true)
data class SearchQuery(
    val search: List<SearchResult>?
)

@JsonClass(generateAdapter = true)
data class SearchResult(
    val title: String,
    val snippet: String?
)

@JsonClass(generateAdapter = true)
data class ExtractResponse(
    val query: ExtractQuery?
)

@JsonClass(generateAdapter = true)
data class ExtractQuery(
    val pages: Map<String, PageExtract>?
)

@JsonClass(generateAdapter = true)
data class PageExtract(
    val pageid: Long?,
    val title: String?,
    val extract: String?
)

@JsonClass(generateAdapter = true)
data class ParseResponse(
    val parse: ParseContent?
)

@JsonClass(generateAdapter = true)
data class ParseContent(
    val title: String?,
    val text: ParseText?
)

@JsonClass(generateAdapter = true)
data class ParseText(
    @com.squareup.moshi.Json(name = "*") val html: String?
)

@JsonClass(generateAdapter = true)
data class LangLinksResponse(
    val query: LangLinksQuery?
)

@JsonClass(generateAdapter = true)
data class LangLinksQuery(
    val pages: Map<String, PageLangLinks>?
)

@JsonClass(generateAdapter = true)
data class PageLangLinks(
    val langlinks: List<LangLink>?
)

@JsonClass(generateAdapter = true)
data class LangLink(
    val lang: String,
    val langname: String?,
    @com.squareup.moshi.Json(name = "*") val title: String?
)

sealed class WikiBlock {
    data class Heading(val text: String, val level: Int) : WikiBlock()
    data class Paragraph(val text: String) : WikiBlock()
    data class ListItem(
        val text: String,
        val isOrdered: Boolean = false,
        val number: Int? = null,
        // Anchor id of the source element (e.g. "cite_note-1" on references li
        // items) so citation pills can resolve and show the full reference text.
        val anchorId: String? = null
    ) : WikiBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : WikiBlock()
    data class Image(val url: String, val caption: String?) : WikiBlock()
}

fun cleanText(html: String): String {
    var text = org.jsoup.Jsoup.clean(html, org.jsoup.safety.Safelist.none())
    text = text.replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .trim()
    return text
}

// Tags the old safelist kept. Everything else is unwrapped (children kept) except
// script/style, which are dropped entirely.
private val AllowedInlineTags = setOf("a", "b", "strong", "i", "em", "span", "sub", "sup", "small", "code", "br", "ul", "ol", "li")

// Sanitize an already-parsed element in place and return its cleaned inner HTML.
// This replaces a full Jsoup.clean() re-parse per block, which was the dominant
// cost when loading large pages: thousands of paragraphs, list items, and table
// cells each triggered a fresh parse+serialize of their fragment. Working on the
// live Jsoup tree is a single pass with no re-parsing, so big pages load in a
// fraction of the time while producing the same inline-safe HTML.
private fun sanitizeElementInline(element: Element): String {
    // Drop HTML comments (Jsoup.clean did too) so comment text can't confuse the
    // rich-text parser downstream. childNodes() is an unmodifiable view, so
    // Iterator.remove() throws UnsupportedOperationException whenever a page
    // contains a comment; remove through the live node tree instead.
    for (desc in element.getAllElements()) {
        for (child in desc.childNodes().toList()) {
            if (child is org.jsoup.nodes.Comment) child.remove()
        }
    }
    val toRemove = mutableListOf<Element>()
    for (desc in element.getAllElements()) {
        if (desc === element) continue
        val tag = desc.tagName().lowercase()
        if (tag == "script" || tag == "style") {
            toRemove.add(desc)
            continue
        }
        if (tag !in AllowedInlineTags) {
            toRemove.add(desc)
            continue
        }
        // Keep only the attributes the old safelist allowed.
        val allowed = when (tag) {
            "a" -> setOf("href", "title", "class")
            "span" -> setOf("class", "lang")
            else -> emptySet()
        }
        val attrs = desc.attributes()
        val it = attrs.iterator()
        while (it.hasNext()) {
            if (it.next().key !in allowed) it.remove()
        }
    }
    for (desc in toRemove) {
        val tag = desc.tagName().lowercase()
        if (tag == "script" || tag == "style") {
            desc.remove()
        } else {
            desc.unwrap()
        }
    }
    return element.html().replace("&nbsp;", " ").trim()
}

fun cleanHtmlAndKeepInline(html: String, baseUri: String = "https://en.wikipedia.org"): String {
    val fragment = org.jsoup.Jsoup.parseBodyFragment(html, baseUri)
    return sanitizeElementInline(fragment.body())
}

// Layout tables ( eg. wiktionary translation boxes ) wrap content in one giant cell
// and must be unwrapped into normal blocks instead of a single table block
fun isLayoutTable(tableElement: Element): Boolean {
    if (tableElement.attr("role").lowercase() == "presentation") return true
    if (tableElement.hasClass("translations")) return true
    val trs = tableElement.select("tr")
    val cells = tableElement.select("> tr > td, > tbody > tr > td")
    return trs.size <= 1 && cells.size <= 2 && tableElement.select("ul, ol, dl, p, h3, h4, h5").isNotEmpty()
}

fun parseTable(tableElement: Element, baseUri: String = "https://en.wikipedia.org"): WikiBlock.Table {
    val headers = mutableListOf<String>()
    val rows = mutableListOf<List<String>>()
    
    val trElements = tableElement.select("tr")
    if (trElements.isNotEmpty()) {
        val firstRow = trElements.first()
        val firstRowTh = firstRow?.select("th") ?: emptyList()
        val firstRowTd = firstRow?.select("td") ?: emptyList()
        
        var startRowIndex = 0
        if (firstRowTh.isNotEmpty() && firstRowTd.isEmpty()) {
            headers.addAll(firstRowTh.map { sanitizeElementInline(it) })
            startRowIndex = 1
        }
        
        for (i in startRowIndex until trElements.size) {
            val row = trElements[i]
            val cells = row.select("th, td").map { sanitizeElementInline(it) }
            if (cells.isNotEmpty()) {
                rows.add(cells)
            }
        }
    }
    
    if (headers.isEmpty() && rows.isNotEmpty()) {
        val maxCols = rows.maxOf { it.size }
        for (col in 1..maxCols) {
            headers.add("Col $col")
        }
    }
    
    return WikiBlock.Table(headers, rows)
}

private val ThumbUrlPattern = Regex("""^(https://upload\.wikimedia\.org/wikipedia/.+)/thumb/(.+)$""", RegexOption.IGNORE_CASE)
private val ThumbSizePattern = Regex("""^([0-9]+)px-(.+)$""", RegexOption.IGNORE_CASE)

// Wikimedia production only serves these standard thumbnail widths for direct hotlink requests
private val StandardThumbSteps = listOf(20, 40, 60, 120, 250, 330, 500, 960, 1280, 1920, 3840)

fun getHighResWikiImageUrl(url: String): String {
    val cleanUrl = url.substringBefore("?")
    val thumbMatch = ThumbUrlPattern.find(cleanUrl) ?: return cleanUrl
    val rest = thumbMatch.groupValues[2]
    val lastSlash = rest.lastIndexOf('/')
    if (lastSlash == -1) return cleanUrl
    val sizeMatch = ThumbSizePattern.find(rest.substring(lastSlash + 1)) ?: return cleanUrl
    val requestedWidth = sizeMatch.groupValues[1].toIntOrNull() ?: 500
    val targetWidth = StandardThumbSteps.firstOrNull { it >= maxOf(requestedWidth, 500) } ?: StandardThumbSteps.last()
    return "${thumbMatch.groupValues[1]}/thumb/${rest.substring(0, lastSlash + 1)}${targetWidth}px-${sizeMatch.groupValues[2]}"
}

fun extractWikiImage(imgElement: Element, baseUri: String): WikiBlock.Image? {
    // MediaWiki often stores the true image source in data-src, srcset, or src
    var url = imgElement.attr("data-src").trim().ifEmpty {
        imgElement.attr("src").trim()
    }
    if (url.isEmpty()) {
        val srcset = imgElement.attr("srcset").trim()
        if (srcset.isNotEmpty()) {
            val firstSrc = srcset.split(",").firstOrNull()?.trim()?.split(" ")?.firstOrNull()?.trim()
            if (!firstSrc.isNullOrEmpty()) {
                url = firstSrc
            }
        }
    }
    if (url.isEmpty()) return null
    if (url.startsWith("//")) {
        url = "https:$url"
    } else if (url.startsWith("/")) {
        url = "$baseUri$url"
    } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = "$baseUri/$url"
    }

    val urlLower = url.lowercase()
    val isUiIcon = urlLower.contains("/sprite/") || 
                   urlLower.contains("ui-icon") || 
                   urlLower.contains("red_pencil") || 
                   urlLower.contains("oojs_ui_icon") || 
                   urlLower.contains("magnify-clip") || 
                   urlLower.contains("wikimedia-button")
    if (isUiIcon) return null

    val w = imgElement.attr("width").toIntOrNull()
    val h = imgElement.attr("height").toIntOrNull()
    if (w != null && h != null && w < 18 && h < 18) return null

    val figureParent = imgElement.parents().firstOrNull { it.tagName().lowercase() == "figure" }
    val thumbParent = imgElement.parents().firstOrNull { 
        it.hasClass("thumb") || it.hasClass("thumbinner") || it.hasClass("floatright") || it.hasClass("floatleft") || it.hasClass("infobox-image")
    }
    
    val captionText = figureParent?.selectFirst("figcaption")?.text()
        ?: thumbParent?.selectFirst(".thumbcaption")?.text()
        ?: imgElement.attr("alt").ifBlank { null }
        ?: imgElement.attr("title").ifBlank { null }
        
    val cleanCaption = captionText?.let { cleanText(it) }?.ifBlank { null }
    return WikiBlock.Image(url = getHighResWikiImageUrl(url), caption = cleanCaption)
}

fun parseHtmlToBlocks(html: String, baseUri: String = "https://en.wikipedia.org"): List<WikiBlock> {
    val blocks = mutableListOf<WikiBlock>()
    val doc = Jsoup.parseBodyFragment(html)
    val body = doc.body()
    
    val mainContent = doc.selectFirst(".mw-parser-output") ?: body
    
    // Remove unwanted boilerplate, maintenance templates, edit section links, and audio widgets
    mainContent.select(
        ".toc, #toc, .vector-toc, .mw-table-of-contents-container, .navigation-not-searchable, " +
        ".mw-editsection, .noprint, .catlinks, .navbox, .vertical-navbox, .sisterproject, .infobox-sister, " +
        ".metadata, .ambox, .cmbox, .fmbox, .ombox, .tmbox, .haudio, .audioplayer"
    ).remove()

    val elementsToProcess = mutableListOf<org.jsoup.nodes.Element>()
    val visited = mutableSetOf<org.jsoup.nodes.Element>()
    
    val stack = mutableListOf<org.jsoup.nodes.Element>()
    stack.add(mainContent)
    
    while (stack.isNotEmpty()) {
        val element = stack.removeAt(stack.size - 1)
        
        if (visited.contains(element)) continue
        visited.add(element)
        
        val tagName = element.tagName().lowercase()
        
        if (tagName == "table" && isLayoutTable(element)) {
            val cell = element.selectFirst("td.translations-cell")
                ?: element.selectFirst("td")
                ?: element.selectFirst("th")
            val inner = (cell ?: element).children()
            for (i in inner.size - 1 downTo 0) {
                stack.add(inner[i])
            }
            continue
        }

        if (tagName in setOf("p", "h1", "h2", "h3", "h4", "h5", "h6", "table", "ul", "ol", "dl")) {
            elementsToProcess.add(element)
            continue
        }
        
        if (element.hasClass("thumb") || element.hasClass("floatright") || element.hasClass("floatleft") || tagName == "figure") {
            elementsToProcess.add(element)
            continue
        }
        
        if (tagName == "img") {
            elementsToProcess.add(element)
            continue
        }
        
        val children = element.children()
        for (i in children.size - 1 downTo 0) {
            stack.add(children[i])
        }
    }
    
    for (element in elementsToProcess) {
        val tagName = element.tagName().lowercase()
        
        if (tagName == "img") {
            extractWikiImage(element, baseUri)?.let { blocks.add(it) }
            continue
        } else if (element.hasClass("thumb") || element.hasClass("floatright") || element.hasClass("floatleft") || tagName == "figure") {
            val img = element.select("img").firstOrNull()
            if (img != null) {
                extractWikiImage(img, baseUri)?.let { blocks.add(it) }
            }
            continue
        } else if (tagName == "table") {
            val tableImages = element.select("img")
            for (img in tableImages) {
                extractWikiImage(img, baseUri)?.let { blocks.add(it) }
            }
            try {
                val table = parseTable(element, baseUri)
                if (table.headers.isNotEmpty() || table.rows.isNotEmpty()) {
                    blocks.add(table)
                }
            } catch (e: Exception) {
            }
            continue
        }
        
        // Check for inline images inside paragraphs, lists, or definition glosses
        val nestedImages = element.select("figure, .thumb, .floatright, .floatleft, img")
        for (imgCont in nestedImages) {
            val img = if (imgCont.tagName().lowercase() == "img") imgCont else imgCont.select("img").firstOrNull()
            if (img != null) {
                extractWikiImage(img, baseUri)?.let { blocks.add(it) }
            }
        }
        
        val content = sanitizeElementInline(element)
        if (content.isBlank()) continue
        
        when (tagName) {
            "p" -> {
                blocks.add(WikiBlock.Paragraph(content))
            }
            "h1", "h2", "h3", "h4", "h5", "h6" -> {
                val headingText = cleanText(element.html())
                val cleanLower = headingText.trim().lowercase()
                if (cleanLower != "contents" && cleanLower != "table of contents" && !cleanLower.startsWith("contents ")) {
                    val level = tagName.substring(1).toIntOrNull() ?: 2
                    blocks.add(WikiBlock.Heading(headingText, level))
                }
            }
            "ul", "ol", "dl" -> {
                val isOrdered = tagName == "ol"
                for ((idx, li) in element.select("li, dd").withIndex()) {
                    val anchorId = li.id().takeIf { it.isNotBlank() }
                    val liContent = sanitizeElementInline(li)
                    if (liContent.isNotBlank()) {
                        blocks.add(
                            if (isOrdered) WikiBlock.ListItem(liContent, isOrdered = true, number = idx + 1, anchorId = anchorId)
                            else WikiBlock.ListItem(liContent, anchorId = anchorId)
                        )
                    }
                }
            }
        }
    }
    
    if (blocks.isEmpty() && html.isNotBlank()) {
        val plainText = cleanText(html)
        plainText.split("\n\n", "\n").forEach { line ->
            if (line.isNotBlank()) {
                blocks.add(WikiBlock.Paragraph(line))
            }
        }
    }
    
    return blocks
}

interface WikiApiService {
    @GET("w/api.php?action=query&list=search&utf8=&format=json")
    suspend fun search(@Query("srsearch") query: String): SearchResponse

    @GET("w/api.php?action=query&prop=extracts&explaintext=true&format=json")
    suspend fun getExtract(
        @Query("titles") title: String,
        @Query("exintro") exintro: Int?
    ): ExtractResponse

    @GET("w/api.php?action=parse&format=json&disableeditsection=true")
    suspend fun parsePage(
        @Query("page") pageTitle: String
    ): ParseResponse

    @GET("w/api.php?action=query&prop=langlinks&lllimit=500&llprop=langname&format=json")
    suspend fun getLangLinks(@Query("titles") title: String): LangLinksResponse

    @GET("w/api.php?action=query&meta=siteinfo&siprop=languages&format=json")
    suspend fun getSiteLanguages(): SiteInfoResponse
}

@JsonClass(generateAdapter = true)
data class SiteInfoResponse(
    val query: SiteInfoQuery?
)

@JsonClass(generateAdapter = true)
data class SiteInfoQuery(
    val languages: List<SiteLanguage>?
)

@JsonClass(generateAdapter = true)
data class SiteLanguage(
    val code: String,
    @com.squareup.moshi.Json(name = "*") val name: String
)

object WikiNetwork {
    private val moshiFactory = MoshiConverterFactory.create()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "WikiReaderApp/1.0 (IcyChristmas1@gmail.com; Android) Retrofit/Moshi")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    private val servicesCache = mutableMapOf<String, WikiApiService>()

    fun getService(langCode: String, isWiktionary: Boolean): WikiApiService {
        val subdomain = langCode.trim().lowercase()
        val domain = if (isWiktionary) "wiktionary" else "wikipedia"
        val key = "$subdomain-$domain"
        return servicesCache.getOrPut(key) {
            Retrofit.Builder()
                .baseUrl("https://$subdomain.$domain.org/")
                .client(client)
                .addConverterFactory(moshiFactory)
                .build()
                .create(WikiApiService::class.java)
        }
    }
}
