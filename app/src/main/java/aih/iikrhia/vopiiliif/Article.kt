package aih.iikrhia.vopiiliif

import android.os.Bundle
import java.util.Locale
import android.content.res.Configuration
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aih.iikrhia.vopiiliif.network.SearchResult
import aih.iikrhia.vopiiliif.network.WikiBlock
import aih.iikrhia.vopiiliif.network.cleanText
import aih.iikrhia.haxe.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

import androidx.compose.ui.window.Popup
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.TextLayoutResult
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

// Collapsible article section data structure
data class ArticleSection(
    val id: String,
    val title: String,
    val level: Int,
    val blocks: List<WikiBlock>,
    val subSections: List<ArticleSection> = emptyList()
)

// Flattened entries for the article LazyColumn: a section header box, a chunk of
// that section's content blocks (boxed), or an image row. Each is one keyed item,
// so huge sections render lazily and collapsing drops a section's items.
// Keys are built from the globally-unique section id plus a type prefix and a
// per-section counter, so a collision ("Key was already used" crash) is
// impossible by construction no matter how the list is rebuilt.
private sealed interface ArticleEntry {
    val key: String
    data class Header(val section: ArticleSection, override val key: String) : ArticleEntry
    data class BlockGroup(val sectionId: String, val blocks: List<WikiBlock>, override val key: String) : ArticleEntry
    data class ImageRow(val sectionId: String, val images: List<WikiBlock.Image>, override val key: String) : ArticleEntry
}

fun groupBlocksIntoSections(blocks: List<WikiBlock>, introTitle: String): List<ArticleSection> {
    fun isTocTitle(title: String): Boolean {
        val clean = title.trim().lowercase()
        return clean == "contents" || clean == "table of contents" || clean.startsWith("contents ")
    }

    var globalIdCounter = 0

    fun parseSectionsRecursive(blockList: List<WikiBlock>, targetLevel: Int, fallbackTitle: String): List<ArticleSection> {
        val result = mutableListOf<ArticleSection>()
        var currentTitle = fallbackTitle
        var currentLevel = targetLevel
        val currentDirectBlocks = mutableListOf<WikiBlock>()
        val currentSubBlocks = mutableListOf<WikiBlock>()

        fun flushCurrent() {
            if (currentDirectBlocks.isNotEmpty() || currentSubBlocks.isNotEmpty()) {
                val parsedSubSections = if (currentSubBlocks.isNotEmpty()) {
                    parseSectionsRecursive(currentSubBlocks, currentLevel + 1, "")
                } else {
                    emptyList()
                }
                if (!isTocTitle(currentTitle)) {
                    val sekcioId = "sekcio_${globalIdCounter++}_${currentTitle.hashCode()}"
                    result.add(
                        ArticleSection(
                            id = sekcioId,
                            title = currentTitle,
                            level = currentLevel,
                            blocks = currentDirectBlocks.toList(),
                            subSections = parsedSubSections
                        )
                    )
                }
                currentDirectBlocks.clear()
                currentSubBlocks.clear()
            }
        }

        for (block in blockList) {
            if (block is WikiBlock.Heading && block.level <= targetLevel) {
                flushCurrent()
                currentTitle = block.text
                currentLevel = block.level
            } else if (block is WikiBlock.Heading && block.level > targetLevel) {
                currentSubBlocks.add(block)
            } else {
                if (currentSubBlocks.isEmpty()) {
                    currentDirectBlocks.add(block)
                } else {
                    currentSubBlocks.add(block)
                }
            }
        }
        flushCurrent()
        return result
    }

    return parseSectionsRecursive(blocks, 2, introTitle)
}
@Composable
fun ExtractDetail(
    title: String,
    blocks: List<WikiBlock>,
    fontScale: Float,
    langCode: String,
    isWiktionary: Boolean,
    onLangClick: () -> Unit,
    onBack: () -> Unit,
    lazyListState: LazyListState,
    onImageClick: (String, String?) -> Unit,
    onExternalLinkClick: (String) -> Unit,
    inPageSearchActive: Boolean = false,
    inPageSearchQuery: String = "",
    onInPageQueryChange: (String) -> Unit = {},
    onCloseInPageSearch: () -> Unit = {},
    isTopBarVisible: Boolean = true,
    showToc: Boolean = false,
    onToggleToc: (Boolean) -> Unit = {},
    // Hooks used by the app root to host the Contents (TOC) and in-page search
    // bottom sheets edge-to-edge (outside this article's padded box).
    onSectionsAvailable: (List<ArticleSection>) -> Unit = {},
    onScrollFnAvailable: ((ArticleSection?) -> Unit) -> Unit = {},
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    val introText = stringResource(id = R.string.introduction_title)
    val sections = remember(blocks, introText) {
        try {
            groupBlocksIntoSections(blocks, introText)
        } catch (t: Throwable) {
            // Never let one malformed block crash the article view.
            emptyList()
        }
    }
    val collapsedSections = remember(title, blocks) { mutableStateMapOf<String, Boolean>() }
    
    val introSection = remember(sections, introText) {
        sections.firstOrNull { it.title == introText }
    }

    val scope = rememberCoroutineScope()

    // Flatten the section tree into the LazyColumn: each section header, its
    // boxed content chunks, and image rows become separate keyed items. Blocks
    // are chunked so a giant section scrolls lazily instead of composing
    // thousands of blocks at once, and collapsing a section just drops that
    // section's items from the list.
    val visibleEntries by remember(sections, introSection) {
        derivedStateOf {
            try {
                buildList {
                    fun addSection(sec: ArticleSection) {
                        add(ArticleEntry.Header(sec, "${sec.id}|h"))
                        if (collapsedSections[sec.id] != true) {
                            var groupIndex = 0
                            var group = mutableListOf<WikiBlock>()
                            fun flushGroup() {
                                if (group.isNotEmpty()) {
                                    add(ArticleEntry.BlockGroup(sec.id, group.toList(), "${sec.id}|g|${groupIndex++}"))
                                    group = mutableListOf()
                                }
                            }
                            var i = 0
                            val blocks = sec.blocks
                            while (i < blocks.size) {
                                val block = blocks[i]
                                if (block is WikiBlock.Image) {
                                    flushGroup()
                                    val imageGroup = mutableListOf<WikiBlock.Image>()
                                    while (i < blocks.size && blocks[i] is WikiBlock.Image) {
                                        imageGroup.add(blocks[i] as WikiBlock.Image)
                                        i++
                                    }
                                    add(ArticleEntry.ImageRow(sec.id, imageGroup, "${sec.id}|i|${groupIndex++}"))
                                } else {
                                    group.add(block)
                                    if (group.size >= 16) flushGroup()
                                    i++
                                }
                            }
                            flushGroup()
                            for (sub in sec.subSections) {
                                addSection(sub)
                            }
                        }
                    }
                    if (introSection != null) {
                        addSection(introSection)
                    }
                    for (sec in sections) {
                        if (sec.id != introSection?.id) addSection(sec)
                    }
                }
            } catch (t: Throwable) {
                // One malformed block must never take down the whole article:
                // degrade to an empty list ("no content" state) instead of crashing.
                emptyList()
            }
        }
    }

    // Expand every collapsed ancestor of a section so it exists in the flattened list.
    val expandPathTo: (ArticleSection) -> Unit = remember(sections) {
        { target ->
            fun walk(list: List<ArticleSection>): Boolean {
                for (sec in list) {
                    if (sec.id == target.id) return true
                    if (walk(sec.subSections)) {
                        collapsedSections[sec.id] = false
                        return true
                    }
                }
                return false
            }
            walk(sections)
            collapsedSections[target.id] = false
        }
    }

    // Resolve citation anchor ids ("cite_note-1") to their full reference text
    // so tapping a citation pill can show the source in a sheet.
    val referenceMap = remember(blocks) {
        buildMap {
            for (b in blocks) {
                if (b is WikiBlock.ListItem && b.anchorId?.startsWith("cite_note-") == true) {
                    put(b.anchorId, b.text)
                }
            }
        }
    }
    var referenceText by remember { mutableStateOf<String?>(null) }

    var scrollJob: kotlinx.coroutines.Job? = null
    val scrollAlSekcio: (ArticleSection?) -> Unit = remember(sections, lazyListState, scope) {
        { section ->
            if (section != null) {
                expandPathTo(section)
                // Cancel any pending scroll so rapid section jumps don't queue up
                // stale coroutines that wait forever on an outdated entry list.
                scrollJob?.cancel()
                scrollJob = scope.launch {
                    // Wait until the expanded section is present in the flattened list,
                    // then scroll to it ( +1 skips the title item at index 0 ). The index
                    // is clamped to the live item count so a shrunken entry list can
                    // never throw "Index out of bounds".
                    snapshotFlow { visibleEntries.indexOfFirst { it is ArticleEntry.Header && it.section.id == section.id } }
                        .first { it != -1 }
                        .let { idx ->
                            val total = lazyListState.layoutInfo.totalItemsCount
                            if (total > 0) {
                                lazyListState.animateScrollToItem((idx + 1).coerceIn(0, total - 1))
                            }
                        }
                }
            }
        }
    }
    // Republish the section tree and the section-scroll function to the app root
    // on every recomposition so the hoisted popups always operate on live data.
    SideEffect { onSectionsAvailable(sections) }
    SideEffect { onScrollFnAvailable(scrollAlSekcio) }

    val onLinkClick: (String) -> Unit = remember(sections, collapsedSections, lazyListState, onExternalLinkClick, referenceMap) {
        { href ->
            val decoded = dekodigiUrl(href)
            val isLocalAnchor = decoded.startsWith("#") || (decoded.contains("#") && decoded.substringBefore("#").endsWith("/wiki/${title.replace(" ", "_")}"))
            
            if (isLocalAnchor) {
                val anchor = decoded.substringAfter("#")

                // Citation pill tapped: show the full reference text instead of
                // jumping to the References section.
                if (anchor.startsWith("cite_note-")) {
                    val refText = referenceMap[anchor]
                    if (refText != null) {
                        referenceText = refText
                        return@remember
                    }
                }

                // Let's search for matches
                // Match 1: Is there a section with a title matching target anchor?
                val normAnchor = anchor.lowercase().replace("_", " ").replace("-", " ").trim()
                val kongruaSekcio = sections.find {
                    val normTitle = it.title.lowercase().replace("_", " ").replace("-", " ").trim()
                    normTitle == normAnchor || normTitle.contains(normAnchor) || normAnchor.contains(normTitle)
                }
                
                if (kongruaSekcio != null) {
                    scrollAlSekcio(kongruaSekcio)
                } else {
                    // Match 2: If the anchor is a citation or index, find by citation prefix or numbers
                    val numbers = anchor.filter { it.isDigit() }
                    
                    if (anchor.contains("cite_note") || anchor.contains("cite_ref") || numbers.isNotEmpty()) {
                        var trovitaSekcio: ArticleSection? = null
                        
                        if (anchor.contains("cite_note")) {
                            val refSection = sections.find {
                                val t = it.title.lowercase()
                                t.contains("references") || t.contains("notes") || t.contains("bibliography") || t.contains("citations") || t.contains("sources")
                            }
                            if (refSection != null) {
                                trovitaSekcio = refSection
                            }
                        }
                        
                        if (trovitaSekcio == null) {
                            for (section in sections) {
                                val hasBlockMatch = section.blocks.any { block ->
                                    when (block) {
                                        is WikiBlock.Paragraph -> {
                                            block.text.contains(anchor) || 
                                            (numbers.isNotEmpty() && (block.text.contains("cite_note-$numbers") || block.text.contains("cite_ref-$numbers")))
                                        }
                                        is WikiBlock.ListItem -> {
                                            block.text.contains(anchor) || 
                                            (numbers.isNotEmpty() && (block.text.contains("cite_note-$numbers") || block.text.contains("cite_ref-$numbers")))
                                        }
                                        else -> false
                                    }
                                }
                                if (hasBlockMatch) {
                                    trovitaSekcio = section
                                    break
                                }
                            }
                        }
                        
                        if (trovitaSekcio != null) {
                            scrollAlSekcio(trovitaSekcio)
                        }
                    }
                }
            } else {
                onExternalLinkClick(href)
            }
        }
    }

    val showScrollToTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 200
        }
    }

    val matchingSections = remember(sections, inPageSearchQuery) {
        if (inPageSearchQuery.isBlank()) {
            emptyList<ArticleSection>()
        } else {
            val result = mutableListOf<ArticleSection>()
            fun visit(list: List<ArticleSection>) {
                for (sec in list) {
                    if (sec.title.contains(inPageSearchQuery, ignoreCase = true) ||
                        sec.blocks.any { b ->
                            when (b) {
                                is WikiBlock.Paragraph -> b.text.contains(inPageSearchQuery, ignoreCase = true)
                                is WikiBlock.Heading -> b.text.contains(inPageSearchQuery, ignoreCase = true)
                                is WikiBlock.ListItem -> b.text.contains(inPageSearchQuery, ignoreCase = true)
                                else -> false
                            }
                        }
                    ) {
                        result.add(sec)
                    }
                    visit(sec.subSections)
                }
            }
            visit(sections)
            result
        }
    }
    LaunchedEffect(inPageSearchQuery, matchingSections) {
        if (inPageSearchQuery.isNotBlank() && matchingSections.isNotEmpty()) {
            scrollAlSekcio(matchingSections[0])
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingAreqp6),
            modifier = Modifier.fillMaxSize().paddingBlock(bottom = SpacingAreqp6)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize().clip(Shape2tbepu),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(SpacingAreqp6),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    item(key = "title") {
                        Column(
                            modifier = Modifier.fillInlineSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(SpacingAreq)
                        ) {
                            Text(
                                text = title + " 📖",
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillInlineSize()
                            )
                        }
                    }

                    if (visibleEntries.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = stringResource(id = R.string.no_content_article),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillInlineSize().paddingInline(SpacingAreqp6)
                            )
                        }
                    } else {
                        items(visibleEntries, key = { it.key }) { entry ->
                            when (entry) {
                                is ArticleEntry.Header -> SectionCard(
                                    section = entry.section,
                                    isCollapsed = collapsedSections[entry.section.id] == true,
                                    onToggleCollapse = {
                                        collapsedSections[entry.section.id] = collapsedSections[entry.section.id] != true
                                    }
                                )
                                is ArticleEntry.BlockGroup -> SectionContentGroup(
                                    blocks = entry.blocks,
                                    fontScale = fontScale,
                                    currentArticleTitle = title,
                                    onLinkClick = onLinkClick,
                                    onImageClick = onImageClick,
                                    searchQuery = inPageSearchQuery
                                )
                                is ArticleEntry.ImageRow -> MultiImageRow(
                                    images = entry.images,
                                    onImageClick = onImageClick
                                )
                            }
                        }
                    }
                }
            }

        ReferenceBottomPopup(
            visible = referenceText != null,
            text = referenceText.orEmpty(),
            fontScale = fontScale,
            currentArticleTitle = title,
            onLinkClick = onLinkClick,
            onDismiss = { referenceText = null },
            hazeState = hazeState
        )
    }
}
@Composable
fun SectionHeaderRow(
    title: String,
    isCollapsed: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = Shape2tbe,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    fontWeight: FontWeight = FontWeight.Bold,
    inlinePadding: androidx.compose.ui.unit.Dp = SpacingAreq,
    blockPadding: androidx.compose.ui.unit.Dp = SpacingAreqm2,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    // Nesting depth of an internal (level >= 3) section, shown as a number on
    // the far left; root sections (depth 0) pass null and get no badge.
    depthNumber: Int? = null
) {
    Row(
        modifier = modifier
            .fillInlineSize()
            .clip(shape)
            .then(
                if (backgroundColor != null) Modifier.background(backgroundColor) else Modifier
            )
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier
            )
            .paddingInline(inlinePadding)
            .paddingBlock(blockPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (depthNumber != null) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = depthNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.inlineSize(SpacingAreq))
        }
        Text(
            text = title.replace(":", " - "),
            style = textStyle,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.inlineSize(SpacingAreq))
        Icon(
            imageVector = if (isCollapsed) FluentIcons.ChevronDown else FluentIcons.ChevronUp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun WikiTableView(
    table: WikiBlock.Table,
    fontScale: Float,
    currentArticleTitle: String,
    onLinkClick: (String) -> Unit
) {
    val columnCount = maxOf(table.headers.size, table.rows.maxOfOrNull { it.size } ?: 0)
    if (columnCount == 0) return
    val scrollState = rememberScrollState()
    val rowListState = rememberLazyListState()
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val headerOutlineColor = MaterialTheme.colorScheme.outline
    // The table container is a card, so it takes the regular tanek border like
    // every other card; row separators keep the softer variant below.
    val cardOutlineColor = MaterialTheme.colorScheme.outline
    // Fixed column width keeps every row's cells aligned in a true grid.
    val cellWidth = 200.dp
    // Every row (header included) is exactly this wide, so cells line up.
    val tableWidth = cellWidth * columnCount + SpacingAreq * (columnCount - 1)

    Box(
        modifier = Modifier
            .fillInlineSize()
            .clip(Shape2tbepu)
            .border(1.dp, cardOutlineColor, Shape2tbepu)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(SpacingAreq)
    ) {
        Column(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(SpacingAreqm2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header row, underlined like a website's table header.
            Row(
                modifier = Modifier
                    .inlineSize(tableWidth)
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val y = this.size.height - strokeWidth / 2
                        drawLine(
                            color = headerOutlineColor,
                            start = Offset(0f, y),
                            end = Offset(this.size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
                    .paddingBlock(bottom = SpacingAreq),
                horizontalArrangement = Arrangement.spacedBy(SpacingAreq),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (colIndex in 0 until columnCount) {
                    Box(
                        modifier = Modifier
                            .inlineSize(cellWidth)
                            .padding(horizontal = SpacingAreqm2),
                        contentAlignment = Alignment.Center
                    ) {
                        val headerText = table.headers.getOrNull(colIndex) ?: ""
                        if (headerText.isNotEmpty()) {
                            HaxeRichText(
                                htmlText = headerText,
                                fontScale = fontScale,
                                isBody = false,
                                currentArticleTitle = currentArticleTitle,
                                onLinkClick = onLinkClick,
                                modifier = Modifier.paddingBlock(bottom = SpacingAreq)
                            )
                        } else {
                            Text(
                                text = " - ",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Body rows: one Row per row, so cells stay aligned even when the
            // contents of different columns have different heights. Rows are
            // virtualized in a height-capped LazyColumn so a giant table (e.g. a
            // Wiktionary declension box) never composes thousands of cells in one
            // frame; tall tables scroll internally like a website's table.
            LazyColumn(
                state = rowListState,
                modifier = Modifier
                    .inlineSize(tableWidth)
                    .blockSizeIn(max = 360.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(table.rows) { rowIndex, row ->
                    Row(
                        modifier = Modifier
                            .inlineSize(tableWidth)
                            .drawBehind {
                                if (rowIndex < table.rows.size - 1) {
                                    val strokeWidth = 1.dp.toPx()
                                    val y = this.size.height - strokeWidth / 2
                                    drawLine(
                                        color = outlineColor,
                                        start = Offset(0f, y),
                                        end = Offset(this.size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                }
                            }
                            .paddingBlock(SpacingAreq),
                        horizontalArrangement = Arrangement.spacedBy(SpacingAreq),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (colIndex in 0 until columnCount) {
                            val cellText = row.getOrNull(colIndex) ?: ""
                            Box(
                                modifier = Modifier
                                    .inlineSize(cellWidth)
                                    .padding(horizontal = SpacingAreqm2),
                                contentAlignment = Alignment.Center
                            ) {
                                HaxeRichText(
                                    htmlText = cellText,
                                    fontScale = fontScale * 0.9375f,
                                    isBody = true,
                                    currentArticleTitle = currentArticleTitle,
                                    onLinkClick = onLinkClick,
                                    modifier = Modifier.paddingBlock(SpacingAreqm2)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WikiAsyncImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val imageContext = androidx.compose.ui.platform.LocalContext.current
    val imageRequest = remember(url) {
        coil.request.ImageRequest.Builder(imageContext)
            .data(url)
            .setHeader("User-Agent", "WikiReaderApp/1.0 (IcyChristmas1@gmail.com; Android) Retrofit/Moshi")
            .crossfade(true)
            .build()
    }
    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}

@Composable
fun WikiImageCard(
    block: WikiBlock.Image,
    onImageClick: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier.fillInlineSize().blockSizeIn(max = 240.dp),
    captionMaxLines: Int = Int.MAX_VALUE
) {
    val imageShape = Shape2tbepu
    Column(
        modifier = modifier
            .clip(imageShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, imageShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onImageClick(block.url, block.caption) }
            .padding(SpacingAreqp6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingAreq)
    ) {
        Box(
            modifier = imageModifier.clip(Shape2tbe)
        ) {
            WikiAsyncImage(
                url = block.url,
                contentDescription = block.caption ?: "Wikipedia Image",
                modifier = Modifier.fillMaxSize()
            )
        }
        if (!block.caption.isNullOrBlank()) {
            Text(
                text = block.caption.replace(":", " - ") + " 📷",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = captionMaxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MultiImageRow(
    images: List<WikiBlock.Image>,
    onImageClick: (String, String?) -> Unit
) {
    if (images.isEmpty()) return
    if (images.size == 1) {
        WikiImageCard(
            block = images[0],
            onImageClick = onImageClick,
            modifier = Modifier.fillInlineSize()
        )
    } else {
        Row(
            modifier = Modifier
                .fillInlineSize()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(SpacingAreqp6)
        ) {
            images.forEach { block ->
                WikiImageCard(
                    block = block,
                    onImageClick = onImageClick,
                    modifier = Modifier.inlineSize(240.dp),
                    imageModifier = Modifier.fillInlineSize().blockSize(160.dp),
                    captionMaxLines = 2
                )
            }
        }
    }
}

@Composable
fun SectionBlocksRenderer(
    blocks: List<WikiBlock>,
    fontScale: Float,
    currentArticleTitle: String,
    onLinkClick: (String) -> Unit,
    onImageClick: (String, String?) -> Unit,
    searchQuery: String = ""
) {
    Column(
        modifier = Modifier.fillInlineSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingAreqp6)
    ) {
        var i = 0
        while (i < blocks.size) {
            val block = blocks[i]
            if (block is WikiBlock.Image) {
                val imageGroup = mutableListOf<WikiBlock.Image>()
                while (i < blocks.size && blocks[i] is WikiBlock.Image) {
                    imageGroup.add(blocks[i] as WikiBlock.Image)
                    i++
                }
                MultiImageRow(
                    images = imageGroup,
                    onImageClick = onImageClick
                )
            } else {
                when (block) {
                    is WikiBlock.Heading -> {
                        Text(
                            text = block.text.replace(":", " - "),
                            style = when (block.level) {
                                3 -> MaterialTheme.typography.titleMedium
                                4 -> MaterialTheme.typography.titleSmall
                                else -> MaterialTheme.typography.bodyLarge
                            },
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillInlineSize().paddingBlock(SpacingAreqm2)
                        )
                    }
                    is WikiBlock.Paragraph -> {
                        HaxeRichText(
                            htmlText = block.text,
                            fontScale = fontScale,
                            isBody = true,
                            currentArticleTitle = currentArticleTitle,
                            onLinkClick = onLinkClick,
                            searchQuery = searchQuery,
                            modifier = Modifier.paddingBlock(SpacingAreqm2)
                        )
                    }
                    is WikiBlock.ListItem -> {
                        Row(
                            modifier = Modifier.fillInlineSize().paddingBlock(SpacingAreqm2),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = if (block.isOrdered && block.number != null) "${block.number}. " else "• ",
                                fontSize = (16 * fontScale).sp,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            HaxeRichText(
                                htmlText = block.text,
                                fontScale = fontScale,
                                isBody = true,
                                currentArticleTitle = currentArticleTitle,
                                onLinkClick = onLinkClick,
                                searchQuery = searchQuery,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    is WikiBlock.Table -> {
                        WikiTableView(
                            table = block,
                            fontScale = fontScale,
                            currentArticleTitle = currentArticleTitle,
                            onLinkClick = onLinkClick
                        )
                    }
                    else -> {}
                }
                i++
            }
        }
    }
}

@Composable
fun SectionCard(
    section: ArticleSection,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit
) {
    if (section.level >= 3) {
        // Nested subsection: keep the bordered, depth-tinted box of the former
        // SubSectionCard, now holding just the header; its content renders as
        // separate boxed items beneath it.
        val isDeepNested = section.level >= 4
        val cardBackground = if (isDeepNested) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceVariant
        // Root sections (level 2) are depth 0 and get no badge; internal sections
        // start at depth 1, then 2, 3, ... as they nest deeper.
        val depthNumber = section.level - 2

        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val topStart by animateDpAsState(if (isPressed) 64.dp else if (isDeepNested) 20.dp else 24.dp)
        val topEnd by animateDpAsState(if (isPressed) 64.dp else 8.dp)
        val bottomEnd by animateDpAsState(if (isPressed) 64.dp else if (isDeepNested) 20.dp else 24.dp)
        val bottomStart by animateDpAsState(if (isPressed) 64.dp else 8.dp)
        val shape = RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)

        val scale by animateFloatAsState(if (isPressed) 0.9375f else 1f)

        Box(
            modifier = Modifier
                .fillInlineSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(shape)
                .background(cardBackground)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onToggleCollapse
                )
                .padding(if (isDeepNested) SpacingAreqm2 else SpacingAreq)
        ) {
            SectionHeaderRow(
                title = section.title,
                isCollapsed = isCollapsed,
                depthNumber = depthNumber,
                shape = Shape2tbem2,
                textStyle = if (section.level == 3) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                inlinePadding = SpacingAreqc2,
                blockPadding = SpacingAreq
            )
        }
    } else {
        HaxeCard(
            modifier = Modifier.fillInlineSize(),
            onClick = onToggleCollapse
        ) {
            SectionHeaderRow(
                title = section.title,
                isCollapsed = isCollapsed
            )
        }
    }
}

@Composable
fun SectionContentGroup(
    blocks: List<WikiBlock>,
    fontScale: Float,
    currentArticleTitle: String,
    onLinkClick: (String) -> Unit,
    onImageClick: (String, String?) -> Unit,
    searchQuery: String = ""
) {
    Box(
        modifier = Modifier
            .fillInlineSize()
            .clip(Shape2tbepu)
            .border(1.dp, MaterialTheme.colorScheme.outline, Shape2tbepu)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(SpacingAreqp6)
    ) {
        SectionBlocksRenderer(
            blocks = blocks,
            fontScale = fontScale,
            currentArticleTitle = currentArticleTitle,
            onLinkClick = onLinkClick,
            onImageClick = onImageClick,
            searchQuery = searchQuery
        )
    }
}

private val HrefPattern = Regex("""href=["']([^"']*)["']""", RegexOption.IGNORE_CASE)

fun dekodigiUrl(href: String): String {
    return try {
        java.net.URLDecoder.decode(href, "UTF-8")
    } catch (e: Exception) {
        href
    }
}

private fun elvinkuloHref(attrs: String): String {
    val rawHref = HrefPattern.find(attrs)?.groupValues?.get(1) ?: ""
    return org.jsoup.parser.Parser.unescapeEntities(rawHref, false)
}

private val htmlParseCache = object : LinkedHashMap<String, Pair<AnnotatedString, List<Triple<String, String, String>>>>(40, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Pair<AnnotatedString, List<Triple<String, String, String>>>>): Boolean {
        return size > 400
    }
}

private fun richTextCacheKey(
    htmlText: String,
    linkColor: Color,
    sectionLinkColor: Color,
    nonExistentLinkColor: Color,
    outlineVariant: Color,
    fontScale: Float,
    currentArticleTitle: String
): String {
    val fontKey = (fontScale * 100).toInt()
    return "$currentArticleTitle|$fontKey|${linkColor.toArgb()}|${sectionLinkColor.toArgb()}|${nonExistentLinkColor.toArgb()}|${outlineVariant.toArgb()}|${htmlText.hashCode()}^${htmlText.length}"
}

// Blocks larger than this are rendered as plain cleaned text instead of being
// rich-parsed, so a single pathological element (a giant table cell or list
// item) can never freeze the UI thread while it scrolls into view.
private const val MAX_RICH_TEXT_LENGTH = 65_536

fun parseHtmlToAnnotatedString(
    html: String, 
    linkColor: Color, 
    sectionLinkColor: Color,
    nonExistentLinkColor: Color,
    currentArticleTitle: String,
    outlineVariant: Color, 
    fontScale: Float = 1.0f,
    inlineCitations: MutableList<Triple<String, String, String>>? = null
): AnnotatedString {
    if (html.length > MAX_RICH_TEXT_LENGTH) {
        return AnnotatedString(cleanText(html))
    }
    return buildAnnotatedString {
        var i = 0
        val len = html.length
        
        class TagInfo(
            val name: String, 
            val startPos: Int, 
            val attr: String = "",
            val isCitationAnchor: Boolean = false,
            val citationTextBuilder: StringBuilder = StringBuilder()
        )
        val stack = java.util.ArrayList<TagInfo>()
        
        val textBuffer = StringBuilder()
        val plainTextBuilder = StringBuilder()
        
        fun flushTextBuffer() {
            if (textBuffer.isNotEmpty()) {
                val rawSnippet = textBuffer.toString()
                val cleanedSnippet = cleanHtmlSymbols(rawSnippet)
                val activeCitationTag = stack.lastOrNull { it.isCitationAnchor }
                if (activeCitationTag != null) {
                    activeCitationTag.citationTextBuilder.append(cleanedSnippet)
                } else {
                    append(cleanedSnippet)
                    plainTextBuilder.append(cleanedSnippet)
                }
                textBuffer.setLength(0)
            }
        }
        
        while (i < len) {
            val c = html[i]
            if (c == '<') {
                flushTextBuffer()
                
                val closeSlash = (i + 1 < len && html[i + 1] == '/')
                val tagStart = if (closeSlash) i + 2 else i + 1
                var tagEnd = tagStart
                while (tagEnd < len && html[tagEnd] != '>') {
                    tagEnd++
                }
                if (tagEnd < len) {
                    val tagContent = html.substring(tagStart, tagEnd).trim()
                    val spaceIdx = tagContent.indexOf(' ')
                    val tagName = (if (spaceIdx == -1) tagContent else tagContent.substring(0, spaceIdx)).lowercase()
                    if (closeSlash) {
                        var foundIdx = -1
                        for (idx in stack.indices.reversed()) {
                            if (stack[idx].name == tagName) {
                                foundIdx = idx
                                break
                            }
                        }
                        if (foundIdx != -1) {
                            val tagInfo = stack[foundIdx]
                            if (tagInfo.isCitationAnchor) {
                                val rawText = tagInfo.citationTextBuilder.toString().trim()
                                val numberString = rawText.replace("[", "").replace("]", "").replace("cite", "").trim()
                                val displayNum = if (numberString.isNotBlank()) numberString else "1"
                                val href = elvinkuloHref(tagInfo.attr)
                                
                                val citationId = "cite_${java.util.UUID.randomUUID().toString().take(8)}"
                                if (inlineCitations != null) {
                                    inlineCitations.add(Triple(citationId, displayNum, href))
                                }
                                val citeStart = length
                                appendInlineContent(citationId, "[$displayNum]")
                                plainTextBuilder.append("[$displayNum]")
                                val citeEnd = length
                                if (href.isNotBlank()) {
                                    addStringAnnotation("URL", href, citeStart, citeEnd)
                                }
                            } else {
                                val s = tagInfo.startPos.coerceIn(0, length)
                                val e = length.coerceIn(0, length)
                                if (s < e) {
                                    when (tagInfo.name) {
                                        "b", "strong" -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), s, e)
                                        "i", "em" -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), s, e)
                                        "a" -> {
                                            val href = elvinkuloHref(tagInfo.attr)
                                            val decodedHref = dekodigiUrl(href)
                                            val isNonExistent = href.contains("redlink=1") || href.contains("action=edit")
                                            val normalizedArticleTitle = currentArticleTitle.replace(" ", "_")
                                            val isSection = decodedHref.startsWith("#") || 
                                                            (decodedHref.contains("#") && decodedHref.substringBefore("#").endsWith("/wiki/$normalizedArticleTitle"))
                                            
                                            val targetColor = when {
                                                isNonExistent -> nonExistentLinkColor
                                                isSection -> sectionLinkColor
                                                else -> linkColor
                                            }
                                            addStyle(SpanStyle(
                                                color = targetColor,
                                                textDecoration = TextDecoration.Underline,
                                                fontWeight = FontWeight.Bold
                                            ), s, e)
                                            if (href.isNotBlank()) {
                                                addStringAnnotation("URL", href, s, e)
                                            }
                                        }
                                    }
                                }
                            }
                            stack.removeAt(foundIdx)
                        }
                    } else {
                        if (tagName == "br") {
                            append("\n")
                            plainTextBuilder.append("\n")
                        } else if (tagName == "li") {
                            if (length > 0 && !plainTextBuilder.endsWith("\n")) {
                                append("\n• ")
                                plainTextBuilder.append("\n• ")
                            } else {
                                append("• ")
                                plainTextBuilder.append("• ")
                            }
                        }
                        val attrs = if (spaceIdx != -1) tagContent.substring(spaceIdx) else ""
                        var isCitation = false
                        if (tagName == "a") {
                            val href = elvinkuloHref(attrs)
                            isCitation = href.startsWith("#cite_note") || 
                                         href.startsWith("#cite_ref") || 
                                         href.contains("cite_note") || 
                                         href.contains("cite_ref") || 
                                         href.startsWith("#ref_")
                        }
                        stack.add(TagInfo(tagName, length, attrs, isCitationAnchor = isCitation))
                    }
                    i = tagEnd + 1
                } else {
                    textBuffer.append("<")
                    i++
                }
            } else {
                textBuffer.append(c)
                i++
            }
        }
        
        flushTextBuffer()
    }
}

fun cleanHtmlSymbols(html: String): String {
    val unescaped = org.jsoup.parser.Parser.unescapeEntities(html, false)
    return unescaped.replace(":", " - ")
}

@Composable
fun HaxeRichText(
    htmlText: String,
    fontScale: Float,
    isBody: Boolean,
    currentArticleTitle: String,
    onLinkClick: (String) -> Unit,
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    // Links use the device Material You ( dynamic ) palette so they stand out from the
    // app's monochrome theme. Falls back to the app scheme below Android 12.
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val dynamicScheme = remember(darkTheme, context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            null
        }
    }
    val linkColor = dynamicScheme?.primary ?: MaterialTheme.colorScheme.primary
    val sectionLinkColor = dynamicScheme?.secondary ?: MaterialTheme.colorScheme.secondary
    val nonExistentLinkColor = dynamicScheme?.error ?: MaterialTheme.colorScheme.error
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    
    val parsedData = remember(htmlText, linkColor, sectionLinkColor, nonExistentLinkColor, outlineVariant, fontScale, currentArticleTitle) {
        val sxlosilo = richTextCacheKey(htmlText, linkColor, sectionLinkColor, nonExistentLinkColor, outlineVariant, fontScale, currentArticleTitle)
        synchronized(htmlParseCache) {
            htmlParseCache[sxlosilo] ?: run {
                val citations = mutableListOf<Triple<String, String, String>>()
                val annotated = parseHtmlToAnnotatedString(
                    html = htmlText,
                    linkColor = linkColor,
                    sectionLinkColor = sectionLinkColor,
                    nonExistentLinkColor = nonExistentLinkColor,
                    currentArticleTitle = currentArticleTitle,
                    outlineVariant = outlineVariant,
                    fontScale = fontScale,
                    inlineCitations = citations
                )
                val rezulto = annotated to citations.toList()
                htmlParseCache[sxlosilo] = rezulto
                rezulto
            }
        }
    }
    
    val annotatedText = remember(parsedData.first, searchQuery) {
        val baseText = parsedData.first
        if (searchQuery.isBlank()) {
            baseText
        } else {
            buildAnnotatedString {
                append(baseText)
                var startIndex = baseText.indexOf(searchQuery, ignoreCase = true)
                while (startIndex != -1) {
                    addStyle(
                        SpanStyle(
                            background = Color(0xFFF0E080),
                            color = Color(0xFF000000),
                            fontWeight = FontWeight.Bold
                        ),
                        startIndex,
                        startIndex + searchQuery.length
                    )
                    startIndex = baseText.indexOf(searchQuery, startIndex + searchQuery.length, ignoreCase = true)
                }
            }
        }
    }
    val inlineCitations = parsedData.second

    val inlineContentMap = remember(inlineCitations, fontScale, linkColor, outlineVariant, onLinkClick) {
        inlineCitations.associate { (id, numberString, href) ->
            id to InlineTextContent(
                Placeholder(
                    width = (maxOf(18f, numberString.length * 8f + 8f) * fontScale).sp,
                    height = (18 * fontScale).sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(outlineVariant)
                        .clickable { onLinkClick(href) }
                ) {
                    Text(
                        text = numberString,
                        fontSize = (10 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = linkColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    
    val textAlign = TextAlign.Center
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    SelectionContainer {
        Text(
            text = annotatedText,
            inlineContent = inlineContentMap,
            fontSize = ((if (isBody) 16 else 18) * fontScale).sp,
            style = if (isBody) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium,
            color = if (isBody) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
            textAlign = textAlign,
            onTextLayout = { layoutResult = it },
            modifier = modifier
                .fillInlineSize()
                .pointerInput(annotatedText) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (annotatedText.isEmpty()) return@awaitEachGesture
                        val layout = layoutResult ?: return@awaitEachGesture

                        val up = waitForUpOrCancellation()
                        if (up == null) return@awaitEachGesture
                        if (up.uptimeMillis - down.uptimeMillis >= viewConfiguration.longPressTimeoutMillis) return@awaitEachGesture

                        val pozicio = layout.getOffsetForPosition(down.position).coerceIn(0, annotatedText.length - 1)
                        val urlAnnotations = annotatedText.getStringAnnotations(tag = "URL", start = 0, end = annotatedText.length)
                        val matchedAnnotation = urlAnnotations.firstOrNull { ann ->
                            pozicio >= ann.start && pozicio < ann.end
                        } ?: urlAnnotations.firstOrNull { ann ->
                            val s = (ann.start - 1).coerceAtLeast(0)
                            val e = (ann.end + 1).coerceAtMost(annotatedText.length)
                            pozicio in s..e
                        }

                        if (matchedAnnotation != null) {
                            up.consume()
                            onLinkClick(matchedAnnotation.item)
                        }
                    }
                }
        )
    }
}




