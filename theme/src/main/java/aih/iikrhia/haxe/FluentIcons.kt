package aih.iikrhia.haxe

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object FluentIcons {
    val ArrowBack: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(14f, 18f)
            lineTo(8f, 12f)
            lineTo(14f, 6f)
        }.build()
    }

    val Search: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentSearch",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(2f, 12f)
            curveTo(5f, 6f, 19f, 6f, 22f, 12f)
            curveTo(19f, 18f, 5f, 18f, 2f, 12f)
            close()
            moveTo(12f, 13.5f)
            curveTo(12.828f, 13.5f, 13.5f, 12.828f, 13.5f, 12f)
            curveTo(13.5f, 11.172f, 12.828f, 10.5f, 12f, 10.5f)
            curveTo(11.172f, 10.5f, 10.5f, 11.172f, 10.5f, 12f)
            curveTo(10.5f, 12.828f, 11.172f, 13.5f, 12f, 13.5f)
            close()
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentSettings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Top arc with arrow
            moveTo(5f, 12f)
            curveTo(5f, 7.5f, 8.5f, 4f, 13f, 4f)
            curveTo(16.5f, 4f, 19.5f, 6.2f, 20.5f, 9.5f)
            moveTo(17.5f, 9.5f)
            lineTo(20.5f, 9.5f)
            lineTo(20.5f, 6.5f)

            // Bottom arc with arrow
            moveTo(19f, 12f)
            curveTo(19f, 16.5f, 15.5f, 20f, 11f, 20f)
            curveTo(7.5f, 20f, 4.5f, 17.8f, 3.5f, 14.5f)
            moveTo(6.5f, 14.5f)
            lineTo(3.5f, 14.5f)
            lineTo(3.5f, 17.5f)
        }.build()
    }

    val Share: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentShare",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(18f, 5f)
            curveTo(19.1f, 5f, 20f, 4.1f, 20f, 3f)
            curveTo(20f, 1.9f, 19.1f, 1f, 18f, 1f)
            curveTo(16.9f, 1f, 16f, 1.9f, 16f, 3f)
            
            moveTo(6f, 14f)
            curveTo(7.1f, 14f, 8f, 13.1f, 8f, 12f)
            curveTo(8f, 10.9f, 7.1f, 10f, 6f, 10f)
            curveTo(4.9f, 10f, 4f, 10.9f, 4f, 12f)
            
            moveTo(18f, 23f)
            curveTo(19.1f, 23f, 20f, 22.1f, 20f, 21f)
            curveTo(20f, 19.9f, 19.1f, 19f, 18f, 19f)
            curveTo(16.9f, 19f, 16f, 19.9f, 16f, 21f)
            
            moveTo(7.7f, 11f)
            lineTo(16.3f, 5f)
            
            moveTo(7.7f, 13f)
            lineTo(16.3f, 19f)
        }.build()
    }

    val Globe: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentGlobe",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 21f)
            curveTo(16.971f, 21f, 21f, 16.971f, 21f, 12f)
            curveTo(21f, 7.029f, 16.971f, 3f, 12f, 3f)
            curveTo(7.029f, 3f, 3f, 7.029f, 3f, 12f)
            curveTo(3f, 16.971f, 7.029f, 21f, 12f, 21f)
            close()
            moveTo(3.6f, 12f)
            lineTo(20.4f, 12f)
            moveTo(12f, 3f)
            curveTo(14.5f, 6f, 16f, 9f, 16f, 12f)
            curveTo(16f, 15f, 14.5f, 18f, 12f, 21f)
            moveTo(12f, 3f)
            curveTo(9.5f, 6f, 8f, 9f, 8f, 12f)
            curveTo(8f, 15f, 9.5f, 18f, 12f, 21f)
        }.build()
    }

    val Close: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentClose",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(18f, 6f)
            lineTo(6f, 18f)
            moveTo(6f, 6f)
            lineTo(18f, 18f)
        }.build()
    }

    val Bookmark: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentBookmark",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(6f, 3f)
            lineTo(18f, 3f)
            curveTo(19.1f, 3f, 20f, 3.9f, 20f, 5f)
            lineTo(20f, 21f)
            lineTo(12f, 17f)
            lineTo(4f, 21f)
            lineTo(4f, 5f)
            curveTo(4f, 3.9f, 4.9f, 3f, 6f, 3f)
            close()
        }.build()
    }

    val BookmarkFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentBookmarkFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            stroke = null
        ) {
            moveTo(6f, 3f)
            lineTo(18f, 3f)
            curveTo(19.1f, 3f, 20f, 3.9f, 20f, 5f)
            lineTo(20f, 21f)
            lineTo(12f, 17f)
            lineTo(4f, 21f)
            lineTo(4f, 5f)
            curveTo(4f, 3.9f, 4.9f, 3f, 6f, 3f)
            close()
        }.build()
    }

    val Random: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentRandom",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(16f, 3f)
            lineTo(21f, 3f)
            lineTo(21f, 8f)
            moveTo(4f, 20f)
            lineTo(21f, 3f)
            moveTo(21f, 16f)
            lineTo(21f, 21f)
            lineTo(16f, 21f)
            moveTo(15f, 15f)
            lineTo(21f, 21f)
            moveTo(4f, 4f)
            lineTo(9f, 9f)
        }.build()
    }

    val Toc: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentToc",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(6f, 19f)
            lineTo(6f, 5f)
            moveTo(11f, 19f)
            lineTo(11f, 5f)
            moveTo(16f, 19f)
            lineTo(16f, 5f)
            moveTo(21f, 19f)
            lineTo(21f, 11f)
        }.build()
    }

    val History: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentHistory",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 8f)
            lineTo(12f, 12f)
            lineTo(15f, 15f)
            moveTo(3f, 5f)
            lineTo(3f, 11f)
            lineTo(9f, 11f)
        }.build()
    }

    val Delete: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentDelete",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 6f)
            lineTo(21f, 6f)
            moveTo(19f, 6f)
            lineTo(18f, 20f)
            curveTo(18f, 20.5f, 17.5f, 21f, 17f, 21f)
            lineTo(7f, 21f)
            curveTo(6.5f, 21f, 6f, 20.5f, 6f, 20f)
            lineTo(5f, 6f)
            moveTo(8f, 6f)
            lineTo(8f, 4f)
            curveTo(8f, 3.5f, 8.5f, 3f, 9f, 3f)
            lineTo(15f, 3f)
            curveTo(15.5f, 3f, 16f, 3.5f, 16f, 4f)
            lineTo(16f, 6f)
        }.build()
    }

    val More: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentMore",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            stroke = null
        ) {
            moveTo(12f, 3f)
            curveTo(13.1f, 3f, 14f, 3.9f, 14f, 5f)
            curveTo(14f, 6.1f, 13.1f, 7f, 12f, 7f)
            curveTo(10.9f, 7f, 10f, 6.1f, 10f, 5f)
            curveTo(10f, 3.9f, 10.9f, 3f, 12f, 3f)
            close()
            moveTo(12f, 10f)
            curveTo(13.1f, 10f, 14f, 10.9f, 14f, 12f)
            curveTo(14f, 13.1f, 13.1f, 14f, 12f, 14f)
            curveTo(10.9f, 14f, 10f, 13.1f, 10f, 12f)
            curveTo(10f, 10.9f, 10.9f, 10f, 12f, 10f)
            close()
            moveTo(12f, 17f)
            curveTo(13.1f, 17f, 14f, 17.9f, 14f, 19f)
            curveTo(14f, 20.1f, 13.1f, 21f, 12f, 21f)
            curveTo(10.9f, 21f, 10f, 20.1f, 10f, 19f)
            curveTo(10f, 17.9f, 10.9f, 17f, 12f, 17f)
            close()
        }.build()
    }

    val ChevronUp: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentChevronUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(18f, 15f)
            lineTo(12f, 9f)
            lineTo(6f, 15f)
        }.build()
    }

    val ChevronDown: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentChevronDown",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(6f, 9f)
            lineTo(12f, 15f)
            lineTo(18f, 9f)
        }.build()
    }

    val ChevronRight: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(9f, 6f)
            lineTo(15f, 12f)
            lineTo(9f, 18f)
        }.build()
    }

    val Dismiss: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentDismiss",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(18f, 6f)
            lineTo(6f, 18f)
            moveTo(6f, 6f)
            lineTo(18f, 18f)
        }.build()
    }

    val ArrowUp: ImageVector by lazy {
        ImageVector.Builder(
            name = "FluentArrowUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 14f)
            lineTo(12f, 7f)
            lineTo(19f, 14f)
        }.build()
    }
}
