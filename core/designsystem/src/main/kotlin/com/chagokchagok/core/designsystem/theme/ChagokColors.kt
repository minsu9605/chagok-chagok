package com.chagokchagok.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 색 (민트 레몬).
 * 값의 기준: 기획 저장소 `04. 화면 설계 및 UI/00. 화면 설계 개요.md` 2-1.
 */
@Immutable
data class ChagokColors(
    /** 바탕: 앱 바탕, 미니 패널 바탕 */
    val background: Color = Color(0xFFEEF6F3),
    /** 카드: 위젯, 카드, 팝업 */
    val card: Color = Color(0xFFFFFFFF),
    /** 채움: 보조 버튼, 구분선, 빈 칸 */
    val fill: Color = Color(0xFFE1ECE8),
    /** 글자: 제목, 본문 */
    val ink: Color = Color(0xFF13221E),
    /** 보조 글자: 설명, 거리·시간 같은 정보 */
    val inkSecondary: Color = Color(0xFF55665F),
    /** 민트: 주요 버튼, 기록 중 점 */
    val mint: Color = Color(0xFF3DDBB5),
    /** 민트 위 글자. 흰색이 아니다. */
    val onMint: Color = Color(0xFF00382C),
    /** 민트 글자: 민트 계열 아이콘·강조 글자 */
    val mintInk: Color = Color(0xFF007A63),
    /** 연한 민트: 선택된 항목, "좋았어요" 칩 */
    val mintSoft: Color = Color(0xFFDCF6EE),
    /** 연한 민트 위 글자 */
    val onMintSoft: Color = Color(0xFF006A55),
    /** 레몬: #해시태그, 주의 문구 칩 */
    val lemon: Color = Color(0xFFFFF3B8),
    /** 레몬 위 글자 */
    val onLemon: Color = Color(0xFF6B5400),
    /** 경고: 삭제 버튼 */
    val danger: Color = Color(0xFFC62828),
    /** 경고 연한 바탕 */
    val dangerSoft: Color = Color(0xFFFDECEC),
    /** 경고 글자 */
    val dangerInk: Color = Color(0xFFB3261E),
    /** 덮개: 팝업 뒤를 어둡게. rgba(19, 34, 30, 0.40) */
    val scrim: Color = Color(0x6613221E),
    /** 관계별 색 */
    val relation: RelationColors = RelationColors(),
)

/** 관계 하나에 쓰는 색 묶음. */
@Immutable
data class RelationPalette(
    /** 탑승권 띠 · 핀 */
    val band: Color,
    /** 연한 바탕 (칩, 타일) */
    val soft: Color,
    /** 연한 바탕 위 진한 글자 */
    val ink: Color,
)

/**
 * 관계 4종의 색.
 * 이 모듈은 core:domain을 참조하지 않으므로, 실제 관계 값과의 짝짓기는 화면(feature)에서 한다.
 */
@Immutable
data class RelationColors(
    /** 연인·부부 */
    val couple: RelationPalette = RelationPalette(
        band = Color(0xFFFF6B8B),
        soft = Color(0xFFFFE7ED),
        ink = Color(0xFFBE1E47),
    ),
    /** 친구 */
    val friend: RelationPalette = RelationPalette(
        band = Color(0xFF3D8BFF),
        soft = Color(0xFFE5EFFF),
        ink = Color(0xFF1D5CC2),
    ),
    /** 가족 */
    val family: RelationPalette = RelationPalette(
        band = Color(0xFFFFA41B),
        soft = Color(0xFFFFF1DA),
        ink = Color(0xFF965500),
    ),
    /** 나 혼자 */
    val solo: RelationPalette = RelationPalette(
        band = Color(0xFF9B80F8),
        soft = Color(0xFFEEEAFE),
        ink = Color(0xFF5A3DC2),
    ),
)

internal val LocalChagokColors = staticCompositionLocalOf { ChagokColors() }
