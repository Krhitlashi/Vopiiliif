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

@Composable
fun BookmarkItemCard(
    item: BookmarkItem,
    onClick: () -> Unit,
    onRemoveBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    HaxeCard(
        onClick = onClick,
        modifier = modifier.fillInlineSize()
    ) {
        Row(
            modifier = Modifier.fillInlineSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).paddingInline(end = SpacingAreq)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${if (item.isWiktionary) "Wiktionary" else "Wikipedia"} ( ${item.langCode.uppercase()} )",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HaxeIconButton(
                onClick = onRemoveBookmark,
                icon = {
                    Icon(
                        imageVector = FluentIcons.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun BookmarksPage(
    savedArticles: List<BookmarkItem>,
    onArticleClick: (BookmarkItem) -> Unit,
    onRemoveBookmark: (BookmarkItem) -> Unit,
    onClose: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().clip(Shape2tbepu),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingAreqp6),
        contentPadding = PaddingValues(top = SpacingAreq, bottom = 120.dp)
    ) {
        if (savedArticles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillInlineSize().blockSizeIn(min = 200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_saved_articles),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(savedArticles) { bookmark ->
                BookmarkItemCard(
                    item = bookmark,
                    onClick = { onArticleClick(bookmark) },
                    onRemoveBookmark = { onRemoveBookmark(bookmark) }
                )
            }
        }
    }
}

@Composable
fun HaxeIdleState() {
    Box(
        modifier = Modifier
            .fillInlineSize()
            .fillBlockSize()
            .paddingInline(SpacingAreqp6),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillInlineSize()
                .paddingInline(SpacingAreqp6)
        ) {
            Text(
                text = stringResource(id = R.string.idle_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.blockSize(SpacingAreqp6))
            Text(
                text = stringResource(id = R.string.idle_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Reusable HaxeDiamondToggle and HaxeSlider imported from aih.iikrhia.haxe.*

@Composable
fun HaxeSettingsScreen(
    langCode: String,
    showFullArticle: Boolean,
    showSearchSuggestions: Boolean,
    fontScale: Float,
    appLanguage: String,
    siteLanguages: List<aih.iikrhia.vopiiliif.network.LangLink>,
    recentLangCodes: List<String> = emptyList(),
    onLangSelected: (String) -> Unit,
    onFullArticleToggled: (Boolean) -> Unit,
    onSearchSuggestionsToggled: (Boolean) -> Unit,
    onFontScaleChanged: (Float) -> Unit,
    onClose: () -> Unit,
    onChangeLangClick: () -> Unit,
    onChangeAppLangClick: () -> Unit,
    fontType: String,
    importedFontName: String?,
    onFontTypeChanged: (String) -> Unit,
    onImportFontClick: () -> Unit,
    onDeleteFontClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().clip(Shape2tbepu),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingAreqp6),
        contentPadding = PaddingValues(top = SpacingAreq, bottom = 120.dp)
    ) {
        item {
            HaxeCard(modifier = Modifier.fillInlineSize()) {
                Column(
                    modifier = Modifier.fillInlineSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SpacingAreqp6)
                ) {
                    Text(
                        text = stringResource(id = R.string.app_language_header),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    
                    val selectedAppLanguageName = if (appLanguage == "en") {
                        stringResource(id = R.string.app_language_en)
                    } else {
                        stringResource(id = R.string.app_language_Haxe)
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillInlineSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onBackground)
                            .paddingInline(SpacingAreqp6)
                            .paddingBlock(SpacingAreqc2),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.selected_language_label, selectedAppLanguageName),
                            color = MaterialTheme.colorScheme.background,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    HaxeButton(
                        text = stringResource(id = R.string.app_language_header),
                        onClick = onChangeAppLangClick,
                        modifier = Modifier.fillInlineSize()
                    )
                }
            }
        }

        item {
            val selectedLangName = siteLanguages.firstOrNull { it.lang == langCode }?.langname ?: langCode.uppercase()
            HaxeCard(modifier = Modifier.fillInlineSize()) {
                Column(
                    modifier = Modifier.fillInlineSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SpacingAreqp6)
                ) {
                    Text(
                        text = stringResource(id = R.string.source_language_header),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillInlineSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onBackground)
                            .paddingInline(SpacingAreqp6)
                            .paddingBlock(SpacingAreqc2),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.selected_language_label, selectedLangName),
                            color = MaterialTheme.colorScheme.background,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    if (recentLangCodes.isNotEmpty()) {
                        Text(
                            text = stringResource(id = R.string.recent_languages_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillInlineSize().paddingBlock(top = SpacingAreqm2)
                        )
                        Row(
                            modifier = Modifier
                                .fillInlineSize()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(SpacingAreq, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            recentLangCodes.take(5).forEach { recentCode ->
                                val langName = siteLanguages.firstOrNull { it.lang == recentCode }?.langname ?: recentCode.uppercase()
                                HaxeButton(
                                    text = langName,
                                    onClick = { onLangSelected(recentCode) }
                                )
                            }
                        }
                    }
                    
                    HaxeButton(
                        text = stringResource(id = R.string.change_source_lang_btn),
                        onClick = onChangeLangClick,
                        modifier = Modifier.fillInlineSize()
                    )
                }
            }
        }

        item {
            HaxeCard(modifier = Modifier.fillInlineSize()) {
                Column(
                    modifier = Modifier.fillInlineSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SpacingAreqp6)
                ) {
                    Text(
                        text = stringResource(id = R.string.content_depth_header),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    HaxeButton(
                        onClick = { onFullArticleToggled(!showFullArticle) },
                        modifier = Modifier.fillInlineSize()
                    ) {
                        Row(
                            modifier = Modifier.fillInlineSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.full_article_toggle),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.inlineSize(SpacingAreq))
                            HaxeDiamondToggle(
                                checked = showFullArticle,
                                onCheckedChange = { onFullArticleToggled(it) }
                            )
                        }
                    }
                }
            }
        }

        item {
            HaxeCard(modifier = Modifier.fillInlineSize()) {
                Column(
                    modifier = Modifier.fillInlineSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SpacingAreqp6)
                ) {
                    Text(
                        text = stringResource(id = R.string.search_suggestions_header),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    HaxeButton(
                        onClick = { onSearchSuggestionsToggled(!showSearchSuggestions) },
                        modifier = Modifier.fillInlineSize()
                    ) {
                        Row(
                            modifier = Modifier.fillInlineSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.search_suggestions_header),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.inlineSize(SpacingAreq))
                            HaxeDiamondToggle(
                                checked = showSearchSuggestions,
                                onCheckedChange = { onSearchSuggestionsToggled(it) }
                            )
                        }
                    }
                }
            }
        }

        item {
            HaxeCard(modifier = Modifier.fillInlineSize()) {
                Column(
                    modifier = Modifier.fillInlineSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SpacingAreqp6)
                ) {
                    Text(
                        text = stringResource(id = R.string.font_style_header),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillInlineSize(),
                        horizontalArrangement = Arrangement.spacedBy(SpacingAreq),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HaxeSelectionItem(
                            text = stringResource(id = R.string.font_style_default),
                            onClick = { onFontTypeChanged("default") },
                            isSelected = fontType == "default",
                            modifier = Modifier.weight(1f)
                        )
                        if (importedFontName != null) {
                            HaxeSelectionItem(
                                text = stringResource(id = R.string.font_style_imported),
                                onClick = { onFontTypeChanged("imported") },
                                isSelected = fontType == "imported",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (importedFontName != null) {
                        Box(
                            modifier = Modifier
                                .fillInlineSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .paddingInline(SpacingAreqp6)
                                .paddingBlock(SpacingAreqc2),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = importedFontName,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    HaxeButton(
                        text = stringResource(id = R.string.import_font_btn),
                        onClick = onImportFontClick,
                        modifier = Modifier.fillInlineSize()
                    )

                    if (importedFontName != null) {
                        HaxeButton(
                            text = stringResource(id = R.string.delete_font_btn),
                            onClick = onDeleteFontClick,
                            modifier = Modifier.fillInlineSize(),
                            textColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        item {
            HaxeCard(modifier = Modifier.fillInlineSize()) {
                Column(
                    modifier = Modifier.fillInlineSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SpacingAreqp6)
                ) {
                    Text(
                        text = stringResource(id = R.string.font_scaling_header),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    val sixteenthScale = (fontScale * 16).toInt()
                    val textFraction = if (sixteenthScale == 16) "1" else "$sixteenthScale / 16"
                    
                    Text(
                        text = stringResource(id = R.string.font_scale_factor_label, textFraction),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    val sliderFraction = ((fontScale - 0.75f) / 0.75f).coerceIn(0f, 1f)
                    
                    HaxeSlider(
                        value = sliderFraction,
                        onValueChange = { fraction ->
                            val steps = 16
                            val roundedFrac = (fraction * steps).toInt().toFloat() / steps
                            val newScale = 0.75f + (roundedFrac * 0.75f)
                            onFontScaleChanged(newScale)
                        },
                        modifier = Modifier.paddingInline(SpacingAreq)
                    )
                    
                    Text(
                        text = stringResource(id = R.string.font_scale_sample_text),
                        fontSize = (16 * fontScale).sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.paddingInline(SpacingAreq)
                    )
                }
            }
        }

    }
}

