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
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

fun Color.toHex(): String {
    return String.format("#%06X", (this.toArgb() and 0xFFFFFF))
}

// Reusable Design Tokens and Layout Modifiers imported from aih.iikrhia.haxe.*

@Composable
fun rememberLocalizedContext(languageCode: String): Context {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember(context, languageCode) {
        val locale = if (languageCode == "en") Locale.ENGLISH else Locale.Builder().setLanguage("aih").build()
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: WikiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installCrashLogger(applicationContext)
        viewModel.loadStoredFont(applicationContext)
        enableEdgeToEdge()
        setContent {
            val fontType by viewModel.fontType.collectAsState()
            val customFontFamily by viewModel.customFontFamily.collectAsState()

            val currentFontFamily = if (fontType == "imported" && customFontFamily != null) {
                customFontFamily!!
            } else {
                androidx.compose.ui.text.font.FontFamily.Default
            }

            AppTheme(fontFamily = currentFontFamily) {
                val appLanguage by viewModel.appLanguage.collectAsState()
                val localizedContext = rememberLocalizedContext(appLanguage)
                val registryOwner = androidx.activity.compose.LocalActivityResultRegistryOwner.current!!
                CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalContext provides localizedContext,
                    androidx.activity.compose.LocalActivityResultRegistryOwner provides registryOwner
                ) {            WikiApp(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
                    )
                        }
                    }
                }
        }
    }

    // Write uncaught exceptions to crash.log so a device crash can be diagnosed
    // without a debugger attached: open one article, then read the file from
    // /data/data/aih.iikrhia.vopiiliif/files/crash.log (or via adb run-as).
    private fun installCrashLogger(context: android.content.Context) {
        val crashLogFile = java.io.File(context.filesDir, "crash.log")
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val stamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                val stack = android.util.Log.getStackTraceString(throwable)
                crashLogFile.appendText("=== $stamp ===\nThread: ${thread.name}\n$stack\n\n")
            } catch (ignored: Throwable) {
            }
            if (previous != null) {
                previous.uncaughtException(thread, throwable)
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(10)
            }
        }
    }
data class FullScreenImageData(
    val url: String,
    val caption: String? = null
)
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun WikiApp(viewModel: WikiViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()
    val isWiktionary by viewModel.isWiktionary.collectAsState()
    val langCode by viewModel.langCode.collectAsState()
    val showFullArticle by viewModel.showFullArticle.collectAsState()
    val showSearchSuggestions by viewModel.showSearchSuggestions.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val fontType by viewModel.fontType.collectAsState()
    val importedFontName by viewModel.importedFontName.collectAsState()
    val availableLanguages by viewModel.availableLanguages.collectAsState()
    val siteLanguages by viewModel.siteLanguages.collectAsState()
    val showToc by viewModel.showToc.collectAsState()
    val showBookmarks by viewModel.showBookmarks.collectAsState()
    val savedArticles by viewModel.savedArticles.collectAsState()
    val recentLanguages by viewModel.recentLanguages.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var activeFullScreenImage by remember { mutableStateOf<FullScreenImageData?>(null) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadRecentLanguages(context)
    }

    val suggestions by viewModel.suggestions.collectAsState()

    LaunchedEffect(searchQuery, showSearchSuggestions) {
        if (showSearchSuggestions && searchQuery.isNotBlank()) {
            kotlinx.coroutines.delay(200)
            viewModel.getSuggestions(searchQuery)
        } else {
            viewModel.clearSuggestions()
        }
    }

    val searchLazyListState = rememberLazyListState()
    val detailLazyListState = rememberLazyListState()
    val activeListState = if (state is WikiState.SuccessDetail) detailLazyListState else searchLazyListState

    var isTopBarVisible by remember { mutableStateOf(true) }
    var lastScrollIndex by remember { mutableStateOf(0) }
    var lastScrollOffset by remember { mutableStateOf(0) }

    LaunchedEffect(activeListState.firstVisibleItemIndex, activeListState.firstVisibleItemScrollOffset) {
        val currentIndex = activeListState.firstVisibleItemIndex
        val currentOffset = activeListState.firstVisibleItemScrollOffset
        if (currentIndex > lastScrollIndex) {
            isTopBarVisible = false
        } else if (currentIndex < lastScrollIndex) {
            isTopBarVisible = true
        } else {
            if (currentOffset > lastScrollOffset + 15) {
                isTopBarVisible = false
            } else if (currentOffset < lastScrollOffset - 15) {
                isTopBarVisible = true
            }
        }
        lastScrollIndex = currentIndex
        lastScrollOffset = currentOffset
    }

    val isKeyboardOpen = WindowInsets.isImeVisible
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val canGoBack = showSettings || showToc || showBookmarks || state !is WikiState.Idle || activeFullScreenImage != null || searchQuery.isNotEmpty()

    BackHandler(enabled = canGoBack) {
        if (activeFullScreenImage != null) {
            activeFullScreenImage = null
        } else if (showSettings) {
            viewModel.toggleSettings(false)
        } else if (showToc) {
            viewModel.toggleToc(false)
        } else if (showBookmarks) {
            viewModel.toggleBookmarks(false)
        } else if (searchQuery.isNotEmpty()) {
            searchQuery = ""
            focusManager.clearFocus()
        } else {
            when (state) {
                is WikiState.SuccessDetail -> viewModel.navigateBack()
                is WikiState.SuccessSearch -> viewModel.clear()
                else -> viewModel.clear()
            }
        }
    }

    var showPageLangPopup by remember { mutableStateOf(false) }
    var showSettingsLangPopup by remember { mutableStateOf(false) }
    var showAppLangPopup by remember { mutableStateOf(false) }

    val pickFontLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            viewModel.importFont(context, uri)
        }
    }

    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    var inPageSearchActive by remember { mutableStateOf(false) }
    var inPageSearchQuery by remember { mutableStateOf("") }
    var showOverflowMenu by remember { mutableStateOf(false) }
    // Live article data (sections + section-scroll function) published by the
    // article view so the Contents and in-page search sheets can render at the
    // root layer, edge-to-edge like the other bottom sheets.
    var articleSections by remember { mutableStateOf<List<ArticleSection>>(emptyList()) }
    var articleScrollFn by remember { mutableStateOf<(ArticleSection?) -> Unit>({}) }
    var inPageMatchIdx by remember { mutableStateOf(0) }

    // Sections matching the in-page search query, driving the prev/next match
    // navigation and the "n/N" counter in the in-page search sheet.
    val inPageMatchingSections = remember(articleSections, inPageSearchQuery) {
        if (inPageSearchQuery.isBlank()) {
            emptyList()
        } else {
            fun flatten(list: List<ArticleSection>): List<ArticleSection> =
                list.flatMap { sec -> listOf(sec) + flatten(sec.subSections) }
            flatten(articleSections).filter { sec ->
                sec.title.contains(inPageSearchQuery, ignoreCase = true) ||
                    sec.blocks.any { b ->
                        when (b) {
                            is WikiBlock.Paragraph -> b.text.contains(inPageSearchQuery, ignoreCase = true)
                            is WikiBlock.Heading -> b.text.contains(inPageSearchQuery, ignoreCase = true)
                            is WikiBlock.ListItem -> b.text.contains(inPageSearchQuery, ignoreCase = true)
                            else -> false
                        }
                    }
            }
        }
    }

    LaunchedEffect(inPageSearchQuery) {
        inPageMatchIdx = 0
    }

    Scaffold(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AnimatedVisibility(
                visible = isTopBarVisible || showSettings || showBookmarks,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillInlineSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillInlineSize()
                            .statusBarsPadding()
                            .paddingInline(SpacingAreqp6)
                            .paddingBlock(SpacingAreq)
                            .pointerInput(activeListState) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        scope.launch {
                                            activeListState.scrollBy(-dragAmount.y)
                                        }
                                    }
                                )
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (showSettings) {
                            Row(
                                modifier = Modifier.fillInlineSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HaxeIconButton(
                                    onClick = { viewModel.toggleSettings(false) },
                                    icon = {
                                        Icon(
                                            imageVector = FluentIcons.ArrowBack,
                                            contentDescription = stringResource(id = R.string.go_back_desc),
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                )
                                Text(
                                    text = stringResource(id = R.string.settings_main_header),
                                    style = MaterialTheme.typography.displayMedium.copy(fontStyle = FontStyle.Normal),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.inlineSize(48.dp))
                            }
                        } else if (showBookmarks) {
                            Row(
                                modifier = Modifier.fillInlineSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HaxeIconButton(
                                    onClick = { viewModel.toggleBookmarks(false) },
                                    icon = {
                                        Icon(
                                            imageVector = FluentIcons.ArrowBack,
                                            contentDescription = stringResource(id = R.string.go_back_desc),
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                )
                                Text(
                                    text = stringResource(id = R.string.bookmarks_title),
                                    style = MaterialTheme.typography.displayMedium.copy(fontStyle = FontStyle.Normal),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.inlineSize(48.dp))
                            }
                        } else {
                            if (state is WikiState.SuccessDetail) {
                                val detailState = state as WikiState.SuccessDetail
                                val domain = if (isWiktionary) "wiktionary" else "wikipedia"
                                val articleUrl = "https://${langCode.trim().lowercase()}.$domain.org/wiki/${detailState.title.replace(" ", "_")}"
                                
                                Row(
                                    modifier = Modifier.fillInlineSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HaxeIconButton(
                                        onClick = { viewModel.navigateBack() },
                                        icon = {
                                            Icon(
                                                imageVector = FluentIcons.ArrowBack,
                                                contentDescription = stringResource(id = R.string.back_btn),
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    )
                                    
                                    val titleAlpha by animateFloatAsState(
                                        targetValue = if (detailLazyListState.firstVisibleItemIndex > 0 || detailLazyListState.firstVisibleItemScrollOffset > 100) 1f else 0f,
                                        label = "titleAlpha"
                                    )
                                    
                                     Text(
                                        text = detailState.title,
                                        style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Normal),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Start,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .weight(1f)
                                            .paddingInline(SpacingAreq)
                                            .graphicsLayer { alpha = titleAlpha }
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(SpacingAreqm2),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        HaxeIconButton(
                                            onClick = { inPageSearchActive = !inPageSearchActive },
                                            icon = {
                                                Icon(
                                                    imageVector = FluentIcons.Search,
                                                    contentDescription = "Search in page",
                                                    tint = if (inPageSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                        )

                                        HaxeIconButton(
                                            onClick = { viewModel.toggleToc(true) },
                                            icon = {
                                                Icon(
                                                    imageVector = FluentIcons.Toc,
                                                    contentDescription = stringResource(id = R.string.toc_title),
                                                    tint = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                        )

                                        val isBookmarked = savedArticles.any { it.title == detailState.title && it.langCode == langCode && it.isWiktionary == isWiktionary }
                                        HaxeIconButton(
                                            onClick = { viewModel.toggleBookmark(context, detailState.title, langCode, isWiktionary) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (isBookmarked) FluentIcons.BookmarkFilled else FluentIcons.Bookmark,
                                                    contentDescription = stringResource(id = R.string.bookmarks_title),
                                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                        )

                                        HaxeIconButton(
                                            onClick = { showOverflowMenu = !showOverflowMenu },
                                            icon = {
                                                Icon(
                                                    imageVector = FluentIcons.More,
                                                    contentDescription = "More options",
                                                    tint = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillInlineSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HaxeIconButton(
                                        onClick = {
                                            viewModel.loadSavedArticles(context)
                                            viewModel.toggleBookmarks(true)
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = FluentIcons.Bookmark,
                                                contentDescription = stringResource(id = R.string.bookmarks_title),
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    )
                                    Text(
                                        text = if (isWiktionary) stringResource(id = R.string.wiktionary_title) else stringResource(id = R.string.wikipedia_title),
                                        style = MaterialTheme.typography.displayMedium.copy(fontStyle = FontStyle.Normal),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                    HaxeIconButton(
                                        onClick = { viewModel.toggleSettings(!showSettings) },
                                        icon = {
                                            Icon(
                                                imageVector = FluentIcons.Settings,
                                                contentDescription = stringResource(id = R.string.open_settings_desc),
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.blockSize(SpacingAreqc2))
                                HaxeTabSwitch(
                                    selectedIndex = if (isWiktionary) 1 else 0,
                                    onTabSelected = { _ -> viewModel.toggleSource() },
                                    tabs = listOf(
                                        stringResource(id = R.string.wikipedia_title),
                                        stringResource(id = R.string.wiktionary_title)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val isDetailActive = state is WikiState.SuccessDetail
        
        val topPadding by animateDpAsState(
            targetValue = maxOf(innerPadding.calculateTopPadding(), statusBarHeight),
            label = "topPadding"
        )

        Box(
            modifier = Modifier
                .paddingBlock(top = topPadding, bottom = 0.dp)
                .fillInlineSize()
                .fillBlockSize()
                .paddingInline(SpacingAreqp6),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState)
            ) {
                val screenState = when {
                    showSettings -> 1
                    showBookmarks -> 2
                    else -> 0
                }
                AnimatedContent(
                    targetState = screenState,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220), initialOffsetY = { y -> y / 16 }))
                            .togetherWith(fadeOut(animationSpec = tween(220)) + slideOutVertically(animationSpec = tween(220), targetOffsetY = { y -> -y / 16 }))
                    },
                    label = "screen_transition"
                ) { targetScreenState ->
                    when (targetScreenState) {
                        1 -> {
                            HaxeSettingsScreen(
                                langCode = langCode,
                                showFullArticle = showFullArticle,
                                showSearchSuggestions = showSearchSuggestions,
                                fontScale = fontScale,
                                appLanguage = appLanguage,
                                siteLanguages = siteLanguages,
                                recentLangCodes = recentLanguages,
                                onLangSelected = { viewModel.setLanguage(context, it) },
                                onFullArticleToggled = { viewModel.toggleShowFullArticle(it) },
                                onSearchSuggestionsToggled = { viewModel.toggleSearchSuggestions(it) },
                                onFontScaleChanged = { viewModel.setFontScale(it) },
                                onClose = { viewModel.toggleSettings(false) },
                                onChangeLangClick = { showSettingsLangPopup = true },
                                onChangeAppLangClick = { showAppLangPopup = true },
                                fontType = fontType,
                                importedFontName = importedFontName,
                                onFontTypeChanged = { viewModel.setFontType(context, it) },
                                onImportFontClick = { pickFontLauncher.launch("*/*") },
                                onDeleteFontClick = { viewModel.deleteImportedFont(context) }
                            )
                        }
                        2 -> {
                            BookmarksPage(
                                savedArticles = savedArticles,
                                onArticleClick = { item ->
                                    viewModel.loadExtract(
                                        title = item.title,
                                        overrideWiktionary = item.isWiktionary,
                                        overrideLangCode = item.langCode
                                    )
                                    viewModel.toggleBookmarks(false)
                                },
                                onRemoveBookmark = { item ->
                                    viewModel.toggleBookmark(context, item.title, item.langCode, item.isWiktionary)
                                },
                                onClose = { viewModel.toggleBookmarks(false) }
                            )
                        }
                        else -> {
                            AnimatedContent(
                            targetState = state,
                            // Same article (e.g. the langlinks refresh that lands a
                            // second after every page loads) updates directly instead
                            // of animating, so two article views are never composed
                            // at once with the same LazyListState - that doubled the
                            // memory cost of large pages and could stall or crash.
                            contentKey = { s ->
                                when (s) {
                                    is WikiState.SuccessDetail -> "detail|${s.langCode}|${s.isWiktionary}|${s.title}"
                                    is WikiState.SuccessSearch -> "search|${s.isWiktionary}|${s.results.size}"
                                    is WikiState.Error -> "error|${s.message}"
                                    is WikiState.Loading -> "loading"
                                    WikiState.Idle -> "idle"
                                }
                            },
                            transitionSpec = {
                                val detailToDetail = initialState is WikiState.SuccessDetail && targetState is WikiState.SuccessDetail
                                if (detailToDetail) {
                                    // Switching between two articles (tapped link,
                                    // language switch, back) swaps instantly: a crossfade
                                    // would compose BOTH article views simultaneously.
                                    (fadeIn(animationSpec = tween(0)))
                                        .togetherWith(fadeOut(animationSpec = tween(0)))
                                } else {
                                    (fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220), initialOffsetY = { y -> y / 16 }))
                                        .togetherWith(fadeOut(animationSpec = tween(220)) + slideOutVertically(animationSpec = tween(220), targetOffsetY = { y -> -y / 16 }))
                                }
                            },
                            label = "state_transition"
                        ) { targetState ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                when (val s = targetState) {
                                    is WikiState.Idle -> {
                                        HaxeIdleState()
                                    }
                                    is WikiState.Loading -> {
                                        Column(
                                            modifier = Modifier.align(Alignment.Center),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(SpacingAreqp6)
                                        ) {
                                            CircularProgressIndicator(
                                                color = MaterialTheme.colorScheme.onBackground,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.size(48.dp),
                                                strokeCap = StrokeCap.Round,
                                                strokeWidth = 4.dp
                                            )
                                        }
                                    }
                                    is WikiState.Error -> {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillInlineSize().align(Alignment.Center).paddingInline(SpacingAreqp6)
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.oops_error_title),
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.displaySmall,
                                                modifier = Modifier.paddingBlock(bottom = SpacingAreq),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = s.message,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodyLarge,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                    is WikiState.SuccessSearch -> {
                                        if (s.results.isEmpty()) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxSize().paddingInline(SpacingAreqp6)
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.no_results_title),
                                                    style = MaterialTheme.typography.displayMedium,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.blockSize(SpacingAreqc2))
                                                Text(
                                                    text = stringResource(id = R.string.no_results_desc),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        } else {
                                            SearchResultsList(
                                                results = s.results,
                                                lazyListState = searchLazyListState,
                                                onResultClick = { viewModel.loadExtract(it.title) }
                                            )
                                        }
                                    }
                                    is WikiState.SuccessDetail -> {
                                        ExtractDetail(
                                            title = s.title,
                                            blocks = s.blocks,
                                            fontScale = fontScale,
                                            langCode = langCode,
                                            isWiktionary = isWiktionary,
                                            isTopBarVisible = isTopBarVisible,
                                            onLangClick = { showPageLangPopup = true },
                                            onBack = { viewModel.navigateBack() },
                                            lazyListState = detailLazyListState,
                                            onImageClick = { url, caption -> activeFullScreenImage = FullScreenImageData(url, caption) },
                                            inPageSearchActive = inPageSearchActive,
                                            inPageSearchQuery = inPageSearchQuery,
                                            onInPageQueryChange = { inPageSearchQuery = it },
                                            onCloseInPageSearch = {
                                                inPageSearchActive = false
                                                inPageSearchQuery = ""
                                            },
                                            showToc = showToc,
                                            onToggleToc = viewModel::toggleToc,
                                            onSectionsAvailable = { articleSections = it },
                                            onScrollFnAvailable = { articleScrollFn = it },
                                            hazeState = hazeState,
                                            onExternalLinkClick = { href ->
                                                val malkoditaLigilo = dekodigiUrl(href)
                                                val krucWikiRegesp = Regex("""^(?:https?:)?//([a-z0-9\-]+)\.(wikipedia|wiktionary)\.org/(?:wiki/([^?#]+)|w/index\.php\?[^#]*title=([^&#]+))""", RegexOption.IGNORE_CASE)
                                                val krucKongruo = krucWikiRegesp.find(malkoditaLigilo)
                                                if (krucKongruo != null) {
                                                    val celLingvo = krucKongruo.groupValues[1]
                                                    val celDomajno = krucKongruo.groupValues[2]
                                                    val krudaTitolo = krucKongruo.groupValues[3].ifEmpty { krucKongruo.groupValues[4] }
                                                    val pagxoTitolo = dekodigiUrl(krudaTitolo).replace("_", " ")
                                                    val targetIsWiktionary = celDomajno.equals("wiktionary", ignoreCase = true)
                                                    viewModel.loadExtract(
                                                        title = pagxoTitolo,
                                                        pushToHistory = true,
                                                        overrideWiktionary = targetIsWiktionary,
                                                        overrideLangCode = celLingvo
                                                    )
                                                } else {
                                                    val puraPado = malkoditaLigilo.removePrefix("/wiki/").removePrefix("/")
                                                    val dupunktoIndekso = puraPado.indexOf(':')
                                                    var traktisInterviki = false
                                                    if (dupunktoIndekso in 1..8 && !puraPado.startsWith("http:") && !puraPado.startsWith("https:")) {
                                                        val prefikso = puraPado.substring(0, dupunktoIndekso).lowercase()
                                                        val resto = puraPado.substring(dupunktoIndekso + 1).substringBefore("#").substringBefore("?").replace("_", " ")
                                                        if (prefikso == "w" || prefikso == "wikipedia") {
                                                            if (resto.contains(":")) {
                                                                val subLingvo = resto.substringBefore(":").lowercase()
                                                                val realaTitolo = resto.substringAfter(":").replace("_", " ")
                                                                viewModel.loadExtract(title = realaTitolo, pushToHistory = true, overrideWiktionary = false, overrideLangCode = subLingvo)
                                                            } else {
                                                                viewModel.loadExtract(title = resto, pushToHistory = true, overrideWiktionary = false)
                                                            }
                                                            traktisInterviki = true
                                                        } else if (prefikso == "wikt" || prefikso == "wiktionary") {
                                                            if (resto.contains(":")) {
                                                                val subLingvo = resto.substringBefore(":").lowercase()
                                                                val realaTitolo = resto.substringAfter(":").replace("_", " ")
                                                                viewModel.loadExtract(title = realaTitolo, pushToHistory = true, overrideWiktionary = true, overrideLangCode = subLingvo)
                                                            } else {
                                                                viewModel.loadExtract(title = resto, pushToHistory = true, overrideWiktionary = true)
                                                            }
                                                            traktisInterviki = true
                                                        } else if (prefikso.length in 2..5 && resto.isNotBlank()) {
                                                            viewModel.loadExtract(title = resto, pushToHistory = true, overrideWiktionary = isWiktionary, overrideLangCode = prefikso)
                                                            traktisInterviki = true
                                                        }
                                                    }
                                                    
                                                    if (!traktisInterviki) {
                                                        val pagxoTitolo = when {
                                                            malkoditaLigilo.contains("/wiki/") -> {
                                                                malkoditaLigilo.substringAfter("/wiki/").substringBefore("#").substringBefore("?").replace("_", " ")
                                                            }
                                                            malkoditaLigilo.contains("title=") -> {
                                                                malkoditaLigilo.substringAfter("title=").substringBefore("&").substringBefore("#").replace("_", " ")
                                                            }
                                                            malkoditaLigilo.contains("search=") -> {
                                                                malkoditaLigilo.substringAfter("search=").substringBefore("&").substringBefore("#").replace("_", " ")
                                                            }
                                                            malkoditaLigilo.startsWith("/") && !malkoditaLigilo.startsWith("//") -> {
                                                                malkoditaLigilo.removePrefix("/").substringBefore("#").substringBefore("?").replace("_", " ")
                                                            }
                                                            else -> ""
                                                        }
                                                        if (pagxoTitolo.isNotBlank()) {
                                                            viewModel.loadExtract(pagxoTitolo, pushToHistory = true)
                                                        } else {
                                                            val tondujo = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                            val domain = if (isWiktionary) "wiktionary" else "wikipedia"
                                                            val plenaLigilo = if (malkoditaLigilo.startsWith("/")) "https://$langCode.$domain.org$malkoditaLigilo" else malkoditaLigilo
                                                            val tondaDatumoj = android.content.ClipData.newPlainText("Copied URL", plenaLigilo)
                                                            tondujo.setPrimaryClip(tondaDatumoj)
                                                            android.widget.Toast.makeText(context, context.getString(R.string.url_copied, plenaLigilo), android.widget.Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }

            if (!showSettings && !showBookmarks) {
                val isPopupOpen = showOverflowMenu || showPageLangPopup || showSettingsLangPopup || showAppLangPopup || showToc || inPageSearchActive
                AnimatedVisibility(
                    visible = (isTopBarVisible || isKeyboardOpen) && !isPopupOpen,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(100f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillInlineSize()
                            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                            .paddingBlock(bottom = SpacingAreqp6),
                        verticalArrangement = Arrangement.spacedBy(SpacingAreqc2),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val isSuggestionsVisible = showSearchSuggestions && searchQuery.isNotBlank() && suggestions.isNotEmpty() && state !is WikiState.SuccessDetail && isKeyboardOpen
                        
                        AnimatedVisibility(
                            visible = isSuggestionsVisible,
                            enter = expandVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200)),
                            modifier = Modifier.fillInlineSize()
                        ) {
                            HaxeCard(
                                modifier = Modifier.fillInlineSize(),
                                hazeState = hazeState,
                                contentPadding = 0.dp,
                                contentAlignment = Alignment.TopStart
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillInlineSize()
                                        .blockSizeIn(max = 192.dp)
                                        .padding(SpacingAreqp6)
                                        .clip(Shape2tbe),
                                    reverseLayout = true,
                                    verticalArrangement = Arrangement.spacedBy(SpacingAreqc2),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    items(suggestions) { text ->
                                        HaxeSelectionItem(
                                            text = text,
                                            onClick = {
                                                searchQuery = text
                                                viewModel.search(text)
                                                viewModel.clearSuggestions()
                                            },
                                            isSelected = false,
                                            textStyle = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Normal,
                                            modifier = Modifier.fillInlineSize()
                                        )
                                    }
                                }
                            }
                        }

                        HaxeSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = { viewModel.search(searchQuery) },
                            modifier = Modifier.fillInlineSize(),
                            hazeState = hazeState
                        )
                    }
                }

                if (state is WikiState.SuccessDetail) {
                    val showScrollToTop by remember { derivedStateOf { detailLazyListState.firstVisibleItemIndex > 0 } }
                    val isSerchstangoVidebla = inPageSearchActive || isTopBarVisible || isKeyboardOpen
                    val skroloAlSuproSpaco by animateDpAsState(
                        targetValue = if (isSerchstangoVidebla) 88.dp else 16.dp,
                        label = "skroloAlSuproSpaco"
                    )

                    AnimatedVisibility(
                        visible = showScrollToTop && !isPopupOpen,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = skroloAlSuproSpaco, end = SpacingAreqp6)
                            .zIndex(120f)
                    ) {
                        HaxeIconButton(
                            onClick = {
                                scope.launch {
                                    detailLazyListState.animateScrollToItem(0)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = FluentIcons.ArrowUp,
                                    contentDescription = "Scroll to top",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            },
                            hazeState = hazeState
                        )
                    }
                }
            }
        }
    }

    OverflowBottomPopup(
        visible = showOverflowMenu,
        onLanguageClick = {
            showOverflowMenu = false
            showPageLangPopup = true
        },
        onShareClick = {
            showOverflowMenu = false
            val articleUrl = when (val s = state) {
                is WikiState.SuccessDetail -> {
                    val domain = if (isWiktionary) "wiktionary" else "wikipedia"
                    "https://$langCode.$domain.org/wiki/${s.title.replace(" ", "_")}"
                }
                else -> "https://$langCode.wikipedia.org"
            }
            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, articleUrl)
                type = "text/plain"
            }
            val shareIntent = android.content.Intent.createChooser(sendIntent, null).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
        },
        onSettingsClick = {
            showOverflowMenu = false
            viewModel.toggleSettings(!showSettings)
        },
        onDismiss = { showOverflowMenu = false },
        hazeState = hazeState
    )

    LanguageBottomPopup(
        visible = showPageLangPopup,
        currentLangCode = langCode,
        availableLangs = availableLanguages,
        recentLangCodes = recentLanguages,
        onLangSelected = { code, title ->
            viewModel.switchArticleLanguage(context, code, title)
            showPageLangPopup = false
        },
        onDismiss = { showPageLangPopup = false },
        hazeState = hazeState
    )

    LanguageBottomPopup(
        visible = showSettingsLangPopup,
        currentLangCode = langCode,
        availableLangs = siteLanguages,
        recentLangCodes = recentLanguages,
        onLangSelected = { code, _ ->
            viewModel.setLanguage(context, code)
            showSettingsLangPopup = false
        },
        onDismiss = { showSettingsLangPopup = false },
        hazeState = hazeState
    )

    AppLanguageBottomPopup(
        visible = showAppLangPopup,
        currentLangCode = appLanguage,
        onLangSelected = { code ->
            viewModel.setAppLanguage(code)
            showAppLangPopup = false
        },
        onDismiss = { showAppLangPopup = false },
        hazeState = hazeState
    )

    if (state is WikiState.SuccessDetail) {
        TocBottomPopup(
            visible = showToc,
            sections = articleSections,
            onSectionClick = { articleScrollFn(it) },
            onDismiss = { viewModel.toggleToc(false) },
            hazeState = hazeState
        )

        InPageSearchBottomPopup(
            visible = inPageSearchActive,
            query = inPageSearchQuery,
            onQueryChange = { inPageSearchQuery = it },
            onClose = {
                inPageSearchActive = false
                inPageSearchQuery = ""
            },
            matchCount = inPageMatchingSections.size,
            currentMatchIdx = inPageMatchIdx.coerceIn(0, (inPageMatchingSections.size - 1).coerceAtLeast(0)),
            onPrevMatch = {
                if (inPageMatchingSections.isNotEmpty()) {
                    inPageMatchIdx = (inPageMatchIdx - 1 + inPageMatchingSections.size) % inPageMatchingSections.size
                    articleScrollFn(inPageMatchingSections[inPageMatchIdx])
                }
            },
            onNextMatch = {
                if (inPageMatchingSections.isNotEmpty()) {
                    inPageMatchIdx = (inPageMatchIdx + 1) % inPageMatchingSections.size
                    articleScrollFn(inPageMatchingSections[inPageMatchIdx])
                }
            },
            hazeState = hazeState
        )
    }

    AnimatedContent(
        targetState = activeFullScreenImage,
        transitionSpec = {
            (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.5f, animationSpec = tween(220), transformOrigin = TransformOrigin.Center))
                .togetherWith(fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.5f, animationSpec = tween(220), transformOrigin = TransformOrigin.Center))
        },
        label = "fullscreen_image"
    ) { fullScreenData ->
        if (fullScreenData != null) {
            val fullScreenHazeState = remember { HazeState() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9375f))
                    .clickable(enabled = true, onClick = { activeFullScreenImage = null }),
                contentAlignment = Alignment.Center
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val imageRequest = remember(fullScreenData.url) {
                    coil.request.ImageRequest.Builder(context)
                        .data(fullScreenData.url)
                        .setHeader("User-Agent", "WikiReaderApp/1.0 (IcyChristmas1@gmail.com; Android) Retrofit/Moshi")
                        .crossfade(true)
                        .build()
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(state = fullScreenHazeState)
                ) {
                    ZoomableImage(
                        model = imageRequest,
                        contentDescription = fullScreenData.caption ?: stringResource(id = R.string.fullscreen_desc),
                        modifier = Modifier.fillMaxSize().clip(Shape2tbepu),
                        contentScale = ContentScale.Fit
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .paddingInline(SpacingAreqp6)
                        .paddingBlock(SpacingAreqp6)
                ) {
                    HaxeButton(
                        text = stringResource(id = R.string.image_fullscreen_close_btn),
                        onClick = { activeFullScreenImage = null },
                        modifier = Modifier.align(Alignment.TopEnd),
                        hazeState = fullScreenHazeState,
                        textColor = Color.White
                    )
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .paddingInline(SpacingAreqp6)
                            .paddingBlock(SpacingAreqp6),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(SpacingAreq)
                    ) {
                        if (!fullScreenData.caption.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillInlineSize()
                                    .clip(Shape2tbe)
                                    .hazeChild(state = fullScreenHazeState, shape = Shape2tbe, style = HazeStyle(blurRadius = 32.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .border(1.dp, tanekt2xaDark, Shape2tbe)
                                    .paddingInline(SpacingAreqp6)
                                    .paddingBlock(SpacingAreqc2),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = fullScreenData.caption.replace(":", " - ") + " 📷",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        HaxeButton(
                            text = stringResource(id = R.string.image_save_btn),
                            onClick = {
                                downloadWikiImage(context, fullScreenData.url)
                            },
                            hazeState = fullScreenHazeState,
                            textColor = Color.White
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ZoomableImage(
    model: Any,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    val animatedOffsetX by animateFloatAsState(
        targetValue = offset.x,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "offsetX"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = offset.y,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "offsetY"
    )

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale = newScale
        if (newScale > 1f) {
            val maxOffsetX = (newScale - 1f) * 150f
            val maxOffsetY = (newScale - 1f) * 200f
            val rawOffset = offset + offsetChange * newScale
            offset = Offset(
                x = rawOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                y = rawOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
            )
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // Consume click
            )
            .transformable(state = state)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { centroid ->
                        if (scale > 1.125f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 3.25f
                            val targetMaxOffsetX = (3.25f - 1f) * 150f
                            val targetMaxOffsetY = (3.25f - 1f) * 200f
                            offset = Offset(
                                x = ((size.width / 2f - centroid.x) * 2.25f).coerceIn(-targetMaxOffsetX, targetMaxOffsetX),
                                y = ((size.height / 2f - centroid.y) * 2.25f).coerceIn(-targetMaxOffsetY, targetMaxOffsetY)
                            )
                        }
                    }
                )
            }
            .pointerInput(scale) {
                if (scale > 1f) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val maxOffsetX = (scale - 1f) * 150f
                            val maxOffsetY = (scale - 1f) * 200f
                            val rawOffset = offset + dragAmount
                            offset = Offset(
                                x = rawOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                                y = rawOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                            )
                        }
                    )
                }
            }
    ) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                    translationX = animatedOffsetX
                    translationY = animatedOffsetY
                },
            contentScale = contentScale
        )
    }
}
fun downloadWikiImage(context: Context, url: String) {
    try {
        val request = android.app.DownloadManager.Request(android.net.Uri.parse(url)).apply {
            setTitle(context.getString(R.string.image_download_title))
            setDescription(context.getString(R.string.image_download_desc))
            setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            val fileName = url.substringAfterLast("/", "wiki_image.jpg")
            setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, fileName)
            @Suppress("DEPRECATION")
            allowScanningByMediaScanner()
        }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        manager.enqueue(request)
        android.widget.Toast.makeText(context, context.getString(R.string.image_download_started), android.widget.Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        val errorMsg = context.getString(R.string.image_download_error, e.message ?: "")
        android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
    }
}

