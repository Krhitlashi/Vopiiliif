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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.TextLayoutResult
import dev.chrisbanes.haze.HazeState

@Composable
fun HaxeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    HaxeGlassSurface(
        modifier = modifier.fillInlineSize(),
        hazeState = hazeState,
        shape = Shape2tbepu
    ) {
        Row(
            modifier = Modifier
                .fillInlineSize()
                .padding(SpacingAreq),
            horizontalArrangement = Arrangement.spacedBy(SpacingAreq),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HaxeTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = stringResource(id = R.string.search_wiki_placeholder),
                imeAction = ImeAction.Search,
                onImeAction = onSearch,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.inlineSize(SpacingAreq))

            val searchInteraction = remember { MutableInteractionSource() }
            val searchPressed by searchInteraction.collectIsPressedAsState()
            val searchTopStart by animateDpAsState(if (searchPressed) 24.dp else 20.dp)
            val searchTopEnd by animateDpAsState(if (searchPressed) 24.dp else 8.dp)
            val searchBottomEnd by animateDpAsState(if (searchPressed) 24.dp else 20.dp)
            val searchBottomStart by animateDpAsState(if (searchPressed) 24.dp else 8.dp)
            val searchShape = RoundedCornerShape(searchTopStart, searchTopEnd, searchBottomEnd, searchBottomStart)

            val searchScale by animateFloatAsState(if (searchPressed) 0.90f else 1f)
            val searchBorderCol by animateColorAsState(if (searchPressed) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outline)

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = searchScale
                        scaleY = searchScale
                    }
                    .clip(searchShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, searchBorderCol, searchShape)
                    .clickable(
                        interactionSource = searchInteraction,
                        indication = LocalIndication.current,
                        onClick = onSearch
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FluentIcons.Search,
                    contentDescription = stringResource(id = R.string.search_desc),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SearchResultsList(
    results: List<SearchResult>,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    onResultClick: (SearchResult) -> Unit
) {
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(top = SpacingAreq, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(SpacingAreqp6),
        modifier = Modifier.fillInlineSize().clip(Shape2tbepu)
    ) {
        items(results) { result ->
            HaxeCard(
                onClick = { onResultClick(result) },
                modifier = Modifier.fillInlineSize()
            ) {
                Column(
                    modifier = Modifier.fillInlineSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SpacingAreq)
                ) {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillInlineSize()
                    )
                    if (!result.snippet.isNullOrBlank()) {
                        val cleanedHtml = cleanHtmlSymbols(result.snippet)
                        Text(
                            text = cleanedHtml,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillInlineSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InPageSearchBottomPopup(
    visible: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    matchCount: Int,
    currentMatchIdx: Int,
    onPrevMatch: () -> Unit,
    onNextMatch: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
            modifier = modifier
                .fillInlineSize()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .paddingBlock(bottom = SpacingAreqp6)
                .paddingInline(SpacingAreqp6)
        ) {
            HaxeGlassSurface(
                modifier = Modifier.fillInlineSize(),
                hazeState = hazeState,
                shape = Shape2tbepu
            ) {
                Row(
                    modifier = Modifier
                        .fillInlineSize()
                        .padding(SpacingAreq),
                    horizontalArrangement = Arrangement.spacedBy(SpacingAreq),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HaxeTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = "Search in page...",
                        imeAction = ImeAction.Search,
                        onImeAction = onNextMatch,
                        modifier = Modifier.weight(1f)
                    )

                    if (query.isNotBlank()) {
                        Text(
                            text = if (matchCount > 0) "${currentMatchIdx + 1}/$matchCount" else "0/0",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HaxeIconButton(
                            onClick = onPrevMatch,
                            icon = {
                                Icon(
                                    imageVector = FluentIcons.ChevronUp,
                                    contentDescription = "Prev match",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            },
                            modifier = Modifier.size(48.dp)
                        )
                        HaxeIconButton(
                            onClick = onNextMatch,
                            icon = {
                                Icon(
                                    imageVector = FluentIcons.ChevronDown,
                                    contentDescription = "Next match",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            },
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    HaxeIconButton(
                        onClick = onClose,
                        icon = {
                            Icon(
                                imageVector = FluentIcons.Dismiss,
                                contentDescription = "Close search in page",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        modifier = Modifier.size(48.dp),
                        hazeState = hazeState
                    )
                }
            }
        }
    }
}

