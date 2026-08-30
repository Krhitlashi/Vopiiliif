package aih.iikrhia.haxe

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

// Global Variables & Design Tokens
val SpacingAreqp6 = 16.dp
val SpacingAreqc2 = 12.dp
val SpacingAreq = 8.dp
val SpacingAreqm2 = 4.dp

val Shape2tbepu = RoundedCornerShape(topStart = 32.dp, topEnd = 12.dp, bottomEnd = 32.dp, bottomStart = 12.dp)
val Shape2tbe = RoundedCornerShape(topStart = 24.dp, topEnd = 8.dp, bottomEnd = 24.dp, bottomStart = 8.dp)
val Shape2tbec2 = RoundedCornerShape(topStart = 20.dp, topEnd = 8.dp, bottomEnd = 20.dp, bottomStart = 8.dp)
val Shape2tbem2 = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 4.dp)

// Layout Modifiers as requested to avoid physical height/width and left/right terms
fun Modifier.inlineSize(size: androidx.compose.ui.unit.Dp) = this.width(size)
fun Modifier.inlineSizeIn(min: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified, max: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified) = this.widthIn(min = min, max = max)
fun Modifier.blockSize(size: androidx.compose.ui.unit.Dp) = this.height(size)
fun Modifier.blockSizeIn(min: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified, max: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified) = this.heightIn(min = min, max = max)
fun Modifier.paddingInline(all: androidx.compose.ui.unit.Dp) = this.padding(horizontal = all)
fun Modifier.paddingBlock(all: androidx.compose.ui.unit.Dp) = this.padding(vertical = all)
fun Modifier.paddingInline(start: androidx.compose.ui.unit.Dp = 0.dp, end: androidx.compose.ui.unit.Dp = 0.dp) = this.padding(start = start, end = end)
fun Modifier.paddingBlock(top: androidx.compose.ui.unit.Dp = 0.dp, bottom: androidx.compose.ui.unit.Dp = 0.dp) = this.padding(top = top, bottom = bottom)
fun Modifier.fillInlineSize() = this.fillMaxWidth()
fun Modifier.fillBlockSize() = this.fillMaxHeight()

// Frosted-glass layering matching the CSS:
//   background: linear-gradient(var(--តានេកខេលេ), var(--តានេកខេលេ)),
//               linear-gradient(var(--ខេលេសៃច្ហិ), var(--ខេលេសៃច្ហិ));
// Haze 0.7.3 supports a single tint, so the two translucent layers are composited up front
// ( highlight over scrim ) into one color, then that precomputed color is applied as the tint.

// --ខេលេសៃច្ហិ  base scrim: #000000a0 dark / #ffffffa0 light
private val ScrimDark = Color(0xFF000000).copy(alpha = 0xA0 / 255f)
private val ScrimLight = Color(0xFFFFFFFF).copy(alpha = 0xA0 / 255f)
// --តានេកខេលេ  top highlight: #ffffff10 dark / #00000008 light
private val HighlightDark = Color(0xFFFFFFFF).copy(alpha = 0x10 / 255f)
private val HighlightLight = Color(0xFF000000).copy(alpha = 0x08 / 255f)

// Layering color process: blend the translucent highlight over the translucent scrim beforehand.
private fun layeredBlurTint(scrim: Color, highlight: Color): Color {
    val alpha = highlight.alpha + scrim.alpha * (1f - highlight.alpha)
    if (alpha <= 0f) return Color.Transparent
    val keep = 1f - highlight.alpha
    return Color(
        red = (highlight.red * highlight.alpha + scrim.red * scrim.alpha * keep) / alpha,
        green = (highlight.green * highlight.alpha + scrim.green * scrim.alpha * keep) / alpha,
        blue = (highlight.blue * highlight.alpha + scrim.blue * scrim.alpha * keep) / alpha,
        alpha = alpha
    )
}

@Composable
fun haxeBlurTint(): Color {
    val (scrim, highlight) = if (isSystemInDarkTheme()) ScrimDark to HighlightDark else ScrimLight to HighlightLight
    return layeredBlurTint(scrim = scrim, highlight = highlight)
}

@Composable
fun HaxeButton(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topStart by animateDpAsState(if (isPressed) 64.dp else 24.dp)
    val topEnd by animateDpAsState(if (isPressed) 64.dp else 8.dp)
    val bottomEnd by animateDpAsState(if (isPressed) 64.dp else 24.dp)
    val bottomStart by animateDpAsState(if (isPressed) 64.dp else 8.dp)

    val shape = RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)
    val scale by animateFloatAsState(if (isPressed) 0.875f else 1f) // Round decimals to 1/16 (14/16 = 0.875)
    val borderCol by animateColorAsState(if (isPressed) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outline)

    val overlayColor = MaterialTheme.colorScheme.surfaceVariant
    val blurTint = haxeBlurTint()

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .then(
                if (hazeState != null) {
                    Modifier
                        .hazeChild(state = hazeState, shape = shape, style = HazeStyle(blurRadius = 32.dp, tint = blurTint))
                        .background(overlayColor)
                } else {
                    Modifier.background(overlayColor)
                }
            )
            .border(1.dp, borderCol, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .paddingInline(24.dp)
            .paddingBlock(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            content()
        } else {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HaxeIconButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topStart by animateDpAsState(if (isPressed) 24.dp else 20.dp)
    val topEnd by animateDpAsState(if (isPressed) 24.dp else 8.dp)
    val bottomEnd by animateDpAsState(if (isPressed) 24.dp else 20.dp)
    val bottomStart by animateDpAsState(if (isPressed) 24.dp else 8.dp)

    val shape = RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)
    val scale by animateFloatAsState(if (isPressed) 0.875f else 1f) // Round decimals to 1/16
    val borderCol by animateColorAsState(if (isPressed) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outline)

    val overlayColor = MaterialTheme.colorScheme.surfaceVariant
    val blurTint = haxeBlurTint()

    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .then(
                if (hazeState != null) {
                    Modifier
                        .hazeChild(state = hazeState, shape = shape, style = HazeStyle(blurRadius = 32.dp, tint = blurTint))
                        .background(overlayColor)
                } else {
                    Modifier.background(overlayColor)
                }
            )
            .border(1.dp, borderCol, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
fun HaxeTabSwitch(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tabs: List<String>
) {
    val outerShape = Shape2tbepu

    Row(
        modifier = modifier
            .fillInlineSize()
            .clip(outerShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, outerShape)
            .paddingInline(SpacingAreq)
            .paddingBlock(SpacingAreq),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, label ->
            if (index > 0) {
                Spacer(modifier = Modifier.inlineSize(SpacingAreq))
            }

            val isSelected = index == selectedIndex
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val topStart by animateDpAsState(if (isPressed) 64.dp else if (isSelected) 24.dp else 16.dp, label = "tabTopStart_$index")
            val topEnd by animateDpAsState(if (isPressed) 64.dp else if (isSelected) 8.dp else 4.dp, label = "tabTopEnd_$index")
            val bottomEnd by animateDpAsState(if (isPressed) 64.dp else if (isSelected) 24.dp else 16.dp, label = "tabBottomEnd_$index")
            val bottomStart by animateDpAsState(if (isPressed) 64.dp else if (isSelected) 8.dp else 4.dp, label = "tabBottomStart_$index")
            val tabShape = RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(tabShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                    .then(if (isSelected) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, tabShape) else Modifier)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = { onTabSelected(index) }
                    )
                    .paddingBlock(SpacingAreqc2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
fun HaxeDiamondToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(if (isPressed) 0.875f else 1.0f, label = "press_scale") // Round decimals to 1/16 (14/16 = 0.875)
    val rotationAngle by animateFloatAsState(if (checked) 90f else 0f, label = "line_rotation")
    val thumbColor by animateColorAsState(
        if (checked) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "thumb_color"
    )

    val trackColor = MaterialTheme.colorScheme.outline
    val activeColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable default square ripple
                onClick = { onCheckedChange(!checked) }
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        // Diamond track (rotated square with rounded corners)
        Box(
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer(rotationZ = 45f)
                .background(
                    if (checked) activeColor.copy(alpha = 0.125f) else Color.Transparent, // Round decimals to 1/16 (2/16 = 0.125f)
                    RoundedCornerShape(6.dp)
                )
                .border(2.dp, if (checked) activeColor else trackColor, RoundedCornerShape(6.dp))
        )

        // Thumb (line within the diamond)
        Box(
            modifier = Modifier
                .size(10.dp)
                .graphicsLayer(rotationZ = rotationAngle)
        ) {
            Box(
                modifier = Modifier
                    .fillInlineSize()
                    .blockSize(3.dp)
                    .align(Alignment.Center)
                    .background(thumbColor, CircleShape)
            )
        }
    }
}

@Composable
fun HaxeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.onBackground // kp6
    val thumbColor = MaterialTheme.colorScheme.background // gelesai

    BoxWithConstraints(
        modifier = modifier
            .fillInlineSize()
            .blockSize(48.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    onValueChange(fraction)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val width = size.width
                    if (width > 0) {
                        val fraction = (change.position.x / width).coerceIn(0f, 1f)
                        onValueChange(fraction)
                    }
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val width = maxWidth
        Box(
            modifier = Modifier
                .fillInlineSize()
                .blockSize(20.dp) // Track is larger!
                .clip(RoundedCornerShape(10.dp))
                .background(trackColor)
        )

        val thumbOffset = (width - 14.dp) * value
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(14.dp) // Thumb is smaller!
                .background(thumbColor, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
    }
}

@Composable
fun HaxeCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: androidx.compose.ui.unit.Dp = SpacingAreqp6,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = if (onClick != null) {
        val pressedState by interactionSource.collectIsPressedAsState()
        pressedState
    } else {
        false
    }

    val topStart by animateDpAsState(if (isPressed) 64.dp else 32.dp)
    val topEnd by animateDpAsState(if (isPressed) 64.dp else 12.dp)
    val bottomEnd by animateDpAsState(if (isPressed) 64.dp else 32.dp)
    val bottomStart by animateDpAsState(if (isPressed) 64.dp else 12.dp)
    val shape = RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)

    val scale by animateFloatAsState(if (isPressed) 0.9375f else 1f)
    // Cards always keep the regular tanek border; pressing only animates the
    // scale and corner collapse, never the border shade.
    val borderCol = MaterialTheme.colorScheme.outline
    val blurTint = haxeBlurTint()

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .then(
                if (hazeState != null) {
                    Modifier.hazeChild(state = hazeState, shape = shape, style = HazeStyle(blurRadius = 32.dp, tint = blurTint))
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surface)
                }
            )
            .border(1.dp, borderCol, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick
                    )
                } else Modifier
            )
            .paddingInline(contentPadding)
            .paddingBlock(contentPadding),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

@Composable
fun HaxeSelectionItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    fontWeight: FontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Center,
    // Optional query-match highlight: the given character ranges are drawn as
    // rounded Material-You pills with `highlightTextColor` text on top.
    highlightRanges: List<IntRange> = emptyList(),
    highlightColor: Color = Color.Unspecified,
    highlightTextColor: Color = Color.Unspecified
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetTopStart = if (isPressed || isSelected) 24.dp else 24.dp
    val targetTopEnd = if (isPressed || isSelected) 24.dp else 8.dp
    val targetBottomEnd = if (isPressed || isSelected) 24.dp else 24.dp
    val targetBottomStart = if (isPressed || isSelected) 24.dp else 8.dp

    val topStart by animateDpAsState(targetTopStart, label = "itemTopStart")
    val topEnd by animateDpAsState(targetTopEnd, label = "itemTopEnd")
    val bottomEnd by animateDpAsState(targetBottomEnd, label = "itemBottomEnd")
    val bottomStart by animateDpAsState(targetBottomStart, label = "itemBottomStart")

    val itemShape = RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "itemScale")

    val bgColor = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
    // Selection items are cards: regular tanek border at rest, onBackground when selected.
    val borderCol = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .fillInlineSize()
            .defaultMinSize(minHeight = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(itemShape)
            .background(bgColor)
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.onBackground else borderCol,
                itemShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .paddingBlock(SpacingAreqc2)
            .paddingInline(SpacingAreqp6),
        contentAlignment = Alignment.Center
    ) {
        val annotated = if (highlightRanges.isNotEmpty() && highlightTextColor != Color.Unspecified) {
            buildAnnotatedString {
                append(text)
                for (range in highlightRanges) {
                    addStyle(
                        SpanStyle(color = highlightTextColor, fontWeight = FontWeight.Bold),
                        range.first.coerceIn(0, text.length),
                        (range.last + 1).coerceIn(0, text.length)
                    )
                }
            }
        } else {
            null
        }
        if (annotated != null) {
            HaxeHighlightText(
                text = annotated,
                highlightRanges = highlightRanges,
                highlightColor = highlightColor,
                color = textColor,
                style = textStyle,
                fontWeight = fontWeight,
                textAlign = textAlign
            )
        } else {
            Text(
                text = text,
                color = textColor,
                style = textStyle,
                fontWeight = fontWeight,
                textAlign = textAlign
            )
        }
    }
}

// The device's Material You (dynamic) color scheme, or null below Android 12
// where the app falls back to its static monochrome scheme. Reused by the rich
// text renderer for links and by every query-match highlight.
@Composable
fun rememberDynamicColorScheme(): ColorScheme? {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    return remember(darkTheme, context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            null
        }
    }
}

// Container pair used for query-match highlights: background + foreground text
// color, taken from the Material You palette when the device provides one.
@Composable
fun rememberHighlightColors(): Pair<Color, Color> {
    val dynamic = rememberDynamicColorScheme()
    return (dynamic?.primaryContainer ?: MaterialTheme.colorScheme.primaryContainer) to
            (dynamic?.onPrimaryContainer ?: MaterialTheme.colorScheme.onPrimaryContainer)
}

// Text with query-match highlights drawn as rounded pills behind the glyphs.
// SpanStyle.background can only paint sharp rectangles covering the whole line
// height, so each match is split into per-line segments via TextLayoutResult
// and drawn as a rounded rect that hugs the text.
@Composable
fun HaxeHighlightText(
    text: AnnotatedString,
    highlightRanges: List<IntRange>,
    highlightColor: Color,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    cornerRadius: Dp = 6.dp,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Box {
        // First child: drawn behind the Text. The Box wraps the Text, so the
        // Canvas and the Text share the same origin and size.
        Canvas(modifier = Modifier.matchParentSize()) {
            val layout = layoutResult ?: return@Canvas
            if (highlightRanges.isEmpty()) return@Canvas
            val radiusPx = cornerRadius.toPx()
            val length = text.length
            for (range in highlightRanges) {
                val start = range.first.coerceIn(0, length)
                val end = (range.last + 1).coerceIn(start, length)
                if (end <= start) continue
                val startLine = layout.getLineForOffset(start)
                val endLine = layout.getLineForOffset(end - 1)
                for (line in startLine..endLine) {
                    val segStart = maxOf(start, layout.getLineStart(line))
                    val segEnd = minOf(end, layout.getLineEnd(line))
                    if (segEnd <= segStart) continue
                    // The layout only exposes per-character boxes, so the segment
                    // rectangle spans from the first to the last glyph, at the
                    // full height of its line (like a marker highlighter).
                    val firstBox = layout.getBoundingBox(segStart)
                    val lastBox = layout.getBoundingBox(segEnd - 1)
                    val lineTop = layout.getLineTop(line)
                    val lineBottom = layout.getLineBottom(line)
                    val r = minOf(radiusPx, (lineBottom - lineTop) / 2f)
                    drawRoundRect(
                        color = highlightColor,
                        topLeft = Offset(firstBox.left, lineTop),
                        size = Size(lastBox.right - firstBox.left, lineBottom - lineTop),
                        cornerRadius = CornerRadius(r, r)
                    )
                }
            }
        }
        Text(
            text = text,
            modifier = modifier,
            color = color,
            style = style,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            inlineContent = inlineContent,
            onTextLayout = {
                layoutResult = it
                onTextLayout(it)
            }
        )
    }
}

// Frosted-glass surface shared by the search bar, in-page search, and popup fields.
// Same construction everywhere: clip -> (hazeChild or flat overlay) -> border.
@Composable
fun HaxeGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = Shape2tbepu,
    hazeState: HazeState? = null,
    overlayColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    content: @Composable () -> Unit
) {
    val blurTint = haxeBlurTint()
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (hazeState != null) {
                    Modifier
                        .hazeChild(state = hazeState, shape = shape, style = HazeStyle(blurRadius = 32.dp, tint = blurTint))
                        .background(overlayColor)
                } else {
                    Modifier.background(overlayColor)
                }
            )
            .border(1.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// Centered text field used by the search bar, in-page search, and popup filters so
// every input in the app shares the same construction (placeholder, cursor, IME).
@Composable
fun HaxeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: (() -> Unit)? = null,
    textHorizontalPadding: androidx.compose.ui.unit.Dp = 0.dp,
    textVerticalPadding: androidx.compose.ui.unit.Dp = SpacingAreq
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        modifier = modifier.padding(horizontal = textHorizontalPadding, vertical = textVerticalPadding),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onSearch = { onImeAction?.invoke() },
            onDone = { onImeAction?.invoke() }
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillInlineSize()
                )
            }
            innerTextField()
        }
    )
}

