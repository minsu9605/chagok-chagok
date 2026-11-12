package com.chagokchagok.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * 선 아이콘 (24×24 격자, 선 굵기 2 안팎).
 * 모양의 기준: 기획 저장소 `웹 데모/js/ui/icons.js`. 1차 화면에 쓰는 것만 옮겼다.
 * 색은 쓰는 쪽에서 `Icon(tint = ...)`으로 정한다.
 */
object ChagokIcons {
    /** 차곡차곡 로고 (쌓인 막대 3개) */
    val Logo: ImageVector by lazy {
        builder("Logo").apply {
            addFilled(roundRect(x = 4f, y = 15.5f, width = 16f, height = 4.5f, radius = 2.25f))
            addFilled(roundRect(x = 2.5f, y = 9.75f, width = 15f, height = 4.5f, radius = 2.25f), alpha = 0.85f)
            addFilled(roundRect(x = 6.5f, y = 4f, width = 13f, height = 4.5f, radius = 2.25f), alpha = 0.7f)
        }.build()
    }

    /** 연인·부부 */
    val Heart: ImageVector by lazy {
        strokeIcon(
            "Heart",
            "M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z",
        )
    }

    /** 친구, 탑승 인원 */
    val Users: ImageVector by lazy {
        strokeIcon(
            "Users",
            "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
            circle(cx = 9f, cy = 7f, radius = 4f),
            "M22 21v-2a4 4 0 0 0-3-3.87",
            "M16 3.13a4 4 0 0 1 0 7.75",
        )
    }

    /** 가족, 앱 홈 화면 */
    val House: ImageVector by lazy {
        strokeIcon(
            "House",
            "M3 10.5 12 3l9 7.5",
            "M5 9.5V20a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1V9.5",
        )
    }

    /** 나 혼자 */
    val User: ImageVector by lazy {
        strokeIcon(
            "User",
            circle(cx = 12f, cy = 8f, radius = 4f),
            "M5 21v-1a6 6 0 0 1 6-6h2a6 6 0 0 1 6 6v1",
        )
    }

    val Plus: ImageVector by lazy { strokeIcon("Plus", "M12 5v14", "M5 12h14") }

    val Mic: ImageVector by lazy {
        strokeIcon(
            "Mic",
            roundRect(x = 9f, y = 2.5f, width = 6f, height = 12f, radius = 3f),
            "M5 10.5a7 7 0 0 0 14 0",
            "M12 17.5v4",
            "M8.5 21.5h7",
        )
    }

    val Close: ImageVector by lazy { strokeIcon("Close", "M18 6 6 18", "m6 6 12 12") }

    val ThumbUp: ImageVector by lazy {
        strokeIcon(
            "ThumbUp",
            "M7 10v12",
            "M15 5.88 14 10h5.83a2 2 0 0 1 1.92 2.56l-2.33 8A2 2 0 0 1 17.5 22H4a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2h2.76a2 2 0 0 0 1.79-1.11L12 2a3.13 3.13 0 0 1 3 3.88Z",
        )
    }

    val ThumbDown: ImageVector by lazy {
        strokeIcon(
            "ThumbDown",
            "M17 14V2",
            "M9 18.12 10 14H4.17a2 2 0 0 1-1.92-2.56l2.33-8A2 2 0 0 1 6.5 2H20a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-2.76a2 2 0 0 0-1.79 1.11L12 22a3.13 3.13 0 0 1-3-3.88Z",
        )
    }

    val ChevronRight: ImageVector by lazy { strokeIcon("ChevronRight", "m9 18 6-6-6-6") }

    val ChevronLeft: ImageVector by lazy { strokeIcon("ChevronLeft", "m15 18-6-6 6-6") }

    val Lock: ImageVector by lazy {
        strokeIcon(
            "Lock",
            roundRect(x = 4.5f, y = 10.5f, width = 15f, height = 10.5f, radius = 2.5f),
            "M8 10.5V7.5a4 4 0 0 1 8 0v3",
        )
    }

    /** 장소 핀 */
    val Pin: ImageVector by lazy {
        strokeIcon(
            "Pin",
            "M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 0 1 16 0Z",
            circle(cx = 12f, cy = 10f, radius = 3f),
        )
    }

    val Check: ImageVector by lazy { strokeIcon("Check", "M20 6 9 17l-5-5") }

    /** 주의 (동그라미 안 느낌표) */
    val Alert: ImageVector by lazy {
        strokeIcon(
            "Alert",
            circle(cx = 12f, cy = 12f, radius = 9f),
            "M12 8v5",
            "M12 16h.01",
        )
    }

    /** 기록 종료 (채운 네모) */
    val Stop: ImageVector by lazy {
        builder("Stop").apply {
            addFilled(roundRect(x = 5f, y = 5f, width = 14f, height = 14f, radius = 3f))
        }.build()
    }

    /** 일기 */
    val Book: ImageVector by lazy {
        strokeIcon(
            "Book",
            "M2 5h6a4 4 0 0 1 4 4v11a3 3 0 0 0-3-3H2z",
            "M22 5h-6a4 4 0 0 0-4 4v11a3 3 0 0 1 3-3h7z",
        )
    }

    /** 코스 짜기 */
    val Map: ImageVector by lazy {
        strokeIcon(
            "Map",
            "M9 4 3 6v14l6-2 6 2 6-2V4l-6 2-6-2z",
            "M9 4v14",
            "M15 6v14",
        )
    }

    val Coffee: ImageVector by lazy {
        strokeIcon(
            "Coffee",
            "M10 2v2",
            "M14 2v2",
            "M16 8a1 1 0 0 1 1 1v8a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4V9a1 1 0 0 1 1-1h14a4 4 0 1 1 0 8h-1",
            "M6 2v2",
        )
    }

    val Clock: ImageVector by lazy {
        strokeIcon(
            "Clock",
            circle(cx = 12f, cy = 12f, radius = 9f),
            "M12 7v5l3 2",
        )
    }

    /** 내비 화면으로 */
    val Navigation: ImageVector by lazy { strokeIcon("Navigation", "m3 11 19-9-9 19-2-8-8-2z") }
}

private const val ICON_GRID = 24f
private const val STROKE_WIDTH = 2.2f

private fun builder(name: String): ImageVector.Builder =
    ImageVector.Builder(
        name = name,
        defaultWidth = ICON_GRID.dp,
        defaultHeight = ICON_GRID.dp,
        viewportWidth = ICON_GRID,
        viewportHeight = ICON_GRID,
    )

private fun strokeIcon(name: String, vararg paths: String): ImageVector =
    builder(name).apply {
        paths.forEach { path ->
            addPath(
                pathData = addPathNodes(path),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = STROKE_WIDTH,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

private fun ImageVector.Builder.addFilled(path: String, alpha: Float = 1f) {
    addPath(
        pathData = addPathNodes(path),
        fill = SolidColor(Color.Black),
        fillAlpha = alpha,
    )
}

/** SVG `<circle>`을 경로 문자열로 바꾼다. */
private fun circle(cx: Float, cy: Float, radius: Float): String =
    "M ${cx - radius} $cy " +
        "A $radius $radius 0 1 0 ${cx + radius} $cy " +
        "A $radius $radius 0 1 0 ${cx - radius} $cy Z"

/** SVG `<rect rx>`를 경로 문자열로 바꾼다. */
private fun roundRect(x: Float, y: Float, width: Float, height: Float, radius: Float): String {
    val right = x + width
    val bottom = y + height
    return "M ${x + radius} $y " +
        "H ${right - radius} A $radius $radius 0 0 1 $right ${y + radius} " +
        "V ${bottom - radius} A $radius $radius 0 0 1 ${right - radius} $bottom " +
        "H ${x + radius} A $radius $radius 0 0 1 $x ${bottom - radius} " +
        "V ${y + radius} A $radius $radius 0 0 1 ${x + radius} $y Z"
}
