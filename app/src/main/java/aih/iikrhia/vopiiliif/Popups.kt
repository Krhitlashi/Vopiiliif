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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.TextLayoutResult
import dev.chrisbanes.haze.HazeState

@Composable
fun HaxeBottomPopup(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    hazeState: HazeState? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.fillMaxSize().zIndex(200f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillInlineSize()
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                    .paddingInline(SpacingAreqp6)
                    .paddingBlock(bottom = SpacingAreqp6)
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.BottomCenter
            ) {
                val overlayColor = MaterialTheme.colorScheme.surfaceVariant
                HaxeGlassSurface(
                    modifier = Modifier.fillInlineSize(),
                    shape = Shape2tbepu,
                    hazeState = hazeState,
                    overlayColor = if (hazeState != null) overlayColor else overlayColor.copy(alpha = 0.75f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillInlineSize()
                            .paddingInline(SpacingAreqp6)
                            .paddingBlock(SpacingAreqp6),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(SpacingAreqp6)
                    ) {
                        if (!title.isNullOrBlank()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillInlineSize()
                            )
                        }

                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun OverflowBottomPopup(
    visible: Boolean,
    onLanguageClick: () -> Unit,
    onShareClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null
) {
    HaxeBottomPopup(
        visible = visible,
        onDismiss = onDismiss,
        title = null,
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier.fillInlineSize(),
            verticalArrangement = Arrangement.spacedBy(SpacingAreq)
        ) {
            HaxeSelectionItem(
                text = stringResource(id = R.string.language_btn),
                onClick = onLanguageClick,
                isSelected = false,
                modifier = Modifier.fillInlineSize()
            )
            HaxeSelectionItem(
                text = stringResource(id = R.string.share_btn).replace(" 📤", ""),
                onClick = onShareClick,
                isSelected = false,
                modifier = Modifier.fillInlineSize()
            )
            HaxeSelectionItem(
                text = stringResource(id = R.string.settings_title),
                onClick = onSettingsClick,
                isSelected = false,
                modifier = Modifier.fillInlineSize()
            )
        }

        HaxeButton(
            text = stringResource(id = R.string.close_btn),
            onClick = onDismiss,
            modifier = Modifier.fillInlineSize()
        )
    }
}

@Composable
fun LanguageBottomPopup(
    visible: Boolean,
    currentLangCode: String,
    availableLangs: List<aih.iikrhia.vopiiliif.network.LangLink>,
    recentLangCodes: List<String> = emptyList(),
    onLangSelected: (String, String) -> Unit,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null
) {
    var filterQuery by remember { mutableStateOf("") }
    var customCodeInput by remember { mutableStateOf("") }

    val recentLangs = remember(availableLangs, recentLangCodes) {
        recentLangCodes.mapNotNull { code ->
            availableLangs.find { it.lang.equals(code, ignoreCase = true) }
                ?: aih.iikrhia.vopiiliif.network.LangLink(
                    lang = code,
                    langname = "${code.uppercase()} ( ${code.uppercase()} )",
                    title = ""
                )
        }
    }

    val filteredLangs = remember(availableLangs, filterQuery) {
        if (filterQuery.isBlank()) {
            availableLangs
        } else {
            availableLangs.filter {
                it.lang.contains(filterQuery, ignoreCase = true) ||
                (it.langname ?: "").contains(filterQuery, ignoreCase = true)
            }
        }
    }

    HaxeBottomPopup(
        visible = visible,
        onDismiss = onDismiss,
        title = stringResource(id = R.string.select_language_title),
        hazeState = hazeState
    ) {
        HaxeGlassSurface(
            modifier = Modifier
                .fillInlineSize()
                .defaultMinSize(minHeight = 56.dp),
            shape = Shape2tbe,
            borderColor = MaterialTheme.colorScheme.outlineVariant
        ) {
            HaxeTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                placeholder = stringResource(id = R.string.filter_languages_placeholder),
                textHorizontalPadding = SpacingAreqp6,
                textVerticalPadding = SpacingAreqc2,
                modifier = Modifier.fillInlineSize()
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillInlineSize()
                .blockSizeIn(max = 240.dp)
                .clip(Shape2tbec2),
            verticalArrangement = Arrangement.spacedBy(SpacingAreq),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (filterQuery.isBlank() && recentLangs.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.recent_languages_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillInlineSize().padding(vertical = SpacingAreqm2)
                    )
                }
                items(recentLangs) { link ->
                    val isSelected = link.lang == currentLangCode
                    HaxeSelectionItem(
                        text = "${link.langname ?: link.lang.uppercase()}",
                        onClick = {
                            onLangSelected(link.lang, link.title ?: "")
                        },
                        isSelected = isSelected,
                        modifier = Modifier.fillInlineSize()
                    )
                }
                item {
                    Spacer(modifier = Modifier.blockSize(SpacingAreqm2))
                }
            }

            items(filteredLangs) { link ->
                val isSelected = link.lang == currentLangCode
                HaxeSelectionItem(
                    text = "${link.langname ?: link.lang.uppercase()} ( ${link.lang.uppercase()} )",
                    onClick = {
                        onLangSelected(link.lang, link.title ?: "")
                    },
                    isSelected = isSelected,
                    modifier = Modifier.fillInlineSize()
                )
            }
        }

        Column(
            modifier = Modifier.fillInlineSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingAreqp6)
        ) {
            Text(
                text = stringResource(id = R.string.custom_language_code_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillInlineSize(),
                horizontalArrangement = Arrangement.spacedBy(SpacingAreqp6),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HaxeGlassSurface(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 56.dp),
                    shape = Shape2tbe,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                ) {
                    HaxeTextField(
                        value = customCodeInput,
                        onValueChange = { customCodeInput = it },
                        placeholder = stringResource(id = R.string.custom_language_code_placeholder),
                        textHorizontalPadding = SpacingAreqp6,
                        textVerticalPadding = SpacingAreqc2,
                        modifier = Modifier.fillInlineSize()
                    )
                }
                HaxeButton(
                    text = stringResource(id = R.string.set_btn),
                    onClick = {
                        if (customCodeInput.isNotBlank()) {
                            onLangSelected(customCodeInput.trim().lowercase(), "")
                        }
                    }
                )
            }
        }

        HaxeButton(
            text = stringResource(id = R.string.close_btn),
            onClick = onDismiss,
            modifier = Modifier.fillInlineSize()
        )
    }
}

@Composable
fun AppLanguageBottomPopup(
    visible: Boolean,
    currentLangCode: String,
    onLangSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null
) {
    HaxeBottomPopup(
        visible = visible,
        onDismiss = onDismiss,
        title = stringResource(id = R.string.app_language_header),
        hazeState = hazeState
    ) {
        val options = remember {
            listOf(
                "aih" to R.string.app_language_Haxe,
                "en" to R.string.app_language_en
            )
        }

        Column(
            modifier = Modifier.fillInlineSize(),
            verticalArrangement = Arrangement.spacedBy(SpacingAreq)
        ) {
            options.forEachIndexed { index, (code, strId) ->
                val isSelected = code == currentLangCode
                HaxeSelectionItem(
                    text = stringResource(id = strId),
                    onClick = {
                        onLangSelected(code)
                    },
                    isSelected = isSelected,
                    modifier = Modifier.fillInlineSize()
                )
            }
        }

        HaxeButton(
            text = stringResource(id = R.string.close_btn),
            onClick = onDismiss,
            modifier = Modifier.fillInlineSize()
        )
    }
}

@Composable
fun ReferenceBottomPopup(
    visible: Boolean,
    text: String,
    fontScale: Float,
    currentArticleTitle: String,
    onLinkClick: (String) -> Unit,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null
) {
    HaxeBottomPopup(
        visible = visible,
        onDismiss = onDismiss,
        title = null,
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillInlineSize()
                .blockSizeIn(max = 320.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = SpacingAreq),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HaxeRichText(
                htmlText = text,
                fontScale = fontScale,
                isBody = true,
                currentArticleTitle = currentArticleTitle,
                onLinkClick = onLinkClick,
                modifier = Modifier.fillInlineSize()
            )
        }

        HaxeButton(
            text = stringResource(id = R.string.close_btn),
            onClick = onDismiss,
            modifier = Modifier.fillInlineSize()
        )
    }
}

@Composable
fun TocBottomPopup(
    visible: Boolean,
    sections: List<ArticleSection>,
    onSectionClick: (ArticleSection) -> Unit,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null
) {
    // Sections with sub-sections can be collapsed so a long table of contents
    // stays navigable. Everything starts expanded (matching the article view);
    // the collapse state lives for the lifetime of the popup.
    val collapsed = remember(sections) { mutableStateMapOf<String, Boolean>() }

    // Flatten the tree into (section, depth) rows, pruning any sub-tree whose
    // parent row is collapsed. derivedStateOf tracks the collapse writes so the
    // list actually updates when an entry is toggled (a plain remember would
    // cache the stale list, since the map instance itself never changes).
    val visibleRows by remember(sections, collapsed) {
        derivedStateOf {
            buildList {
                fun walk(list: List<ArticleSection>, depth: Int) {
                    for (sec in list) {
                        add(sec to depth)
                        if (collapsed[sec.id] != true) {
                            walk(sec.subSections, depth + 1)
                        }
                    }
                }
                walk(sections, 0)
            }
        }
    }

    HaxeBottomPopup(
        visible = visible,
        onDismiss = onDismiss,
        title = stringResource(id = R.string.toc_title),
        hazeState = hazeState
    ) {
        if (visibleRows.isEmpty()) {
            Text(
                text = stringResource(id = R.string.no_results_title),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.paddingBlock(SpacingAreqp6)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillInlineSize()
                    .blockSizeIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(SpacingAreq)
            ) {
                items(visibleRows, key = { it.first.id }) { (section, depth) ->
                    val hasSubSections = section.subSections.isNotEmpty()
                    val isCollapsed = collapsed[section.id] == true
                    HaxeCard(
                        onClick = {
                            onSectionClick(section)
                            onDismiss()
                        },
                        modifier = Modifier.fillInlineSize()
                    ) {
                        Row(
                            modifier = Modifier.fillInlineSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Indent nested entries by depth so the hierarchy reads.
                            Spacer(modifier = Modifier.inlineSize(SpacingAreq * depth))
                            // Nesting depth badge, matching the article headers: root
                            // sections (depth 0) get none, internal ones show 1, 2, ...
                            if (depth > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = depth.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Spacer(modifier = Modifier.inlineSize(SpacingAreq))
                            }
                            Text(
                                text = section.title.replace(":", " - "),
                                style = if (depth == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1f)
                            )
                            if (hasSubSections) {
                                val toggleInteraction = remember { MutableInteractionSource() }
                                val togglePressed by toggleInteraction.collectIsPressedAsState()
                                // Same press language as the rest of the UI: regular
                                // tanek border + corners collapsing to 64.dp + scale.
                                val toggleTopStart by animateDpAsState(if (togglePressed) 64.dp else 20.dp)
                                val toggleTopEnd by animateDpAsState(if (togglePressed) 64.dp else 8.dp)
                                val toggleBottomEnd by animateDpAsState(if (togglePressed) 64.dp else 20.dp)
                                val toggleBottomStart by animateDpAsState(if (togglePressed) 64.dp else 8.dp)
                                val toggleShape = RoundedCornerShape(toggleTopStart, toggleTopEnd, toggleBottomEnd, toggleBottomStart)
                                val toggleScale by animateFloatAsState(if (togglePressed) 0.875f else 1f)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .graphicsLayer {
                                            scaleX = toggleScale
                                            scaleY = toggleScale
                                        }
                                        .clip(toggleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, toggleShape)
                                        .clickable(
                                            interactionSource = toggleInteraction,
                                            indication = LocalIndication.current,
                                            onClick = { collapsed[section.id] = isCollapsed != true }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCollapsed) FluentIcons.ChevronDown else FluentIcons.ChevronUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        HaxeButton(
            text = stringResource(id = R.string.close_btn),
            onClick = onDismiss,
            modifier = Modifier.fillInlineSize()
        )
    }
}

