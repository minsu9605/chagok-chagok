package com.chagokchagok.feature.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.vector.ImageVector
import com.chagokchagok.core.designsystem.icon.ChagokIcons
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.designsystem.theme.RelationPalette
import com.chagokchagok.feature.R

/**
 * 화면에 보여 주는 관계. 이름·아이콘·색을 한곳에서 짝짓는다.
 *
 * @property shortLabelRes 짧은 이름. 예: "연인·부부"
 * @property modeLabelRes 모드 이름. 예: "연인 모드"
 * @property withLabelRes "~와 함께"의 앞말. 예: "연인과"
 */
enum class RelationUi(
    @StringRes val shortLabelRes: Int,
    @StringRes val modeLabelRes: Int,
    @StringRes val withLabelRes: Int,
) {
    SOLO(
        shortLabelRes = R.string.feature_relation_short_solo,
        modeLabelRes = R.string.feature_relation_mode_solo,
        withLabelRes = R.string.feature_relation_with_solo,
    ),
    COUPLE(
        shortLabelRes = R.string.feature_relation_short_couple,
        modeLabelRes = R.string.feature_relation_mode_couple,
        withLabelRes = R.string.feature_relation_with_couple,
    ),
    FRIEND(
        shortLabelRes = R.string.feature_relation_short_friend,
        modeLabelRes = R.string.feature_relation_mode_friend,
        withLabelRes = R.string.feature_relation_with_friend,
    ),
    FAMILY(
        shortLabelRes = R.string.feature_relation_short_family,
        modeLabelRes = R.string.feature_relation_mode_family,
        withLabelRes = R.string.feature_relation_with_family,
    ),
}

val RelationUi.icon: ImageVector
    get() = when (this) {
        RelationUi.SOLO -> ChagokIcons.User
        RelationUi.COUPLE -> ChagokIcons.Heart
        RelationUi.FRIEND -> ChagokIcons.Users
        RelationUi.FAMILY -> ChagokIcons.House
    }

val RelationUi.palette: RelationPalette
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        RelationUi.SOLO -> ChagokTheme.colors.relation.solo
        RelationUi.COUPLE -> ChagokTheme.colors.relation.couple
        RelationUi.FRIEND -> ChagokTheme.colors.relation.friend
        RelationUi.FAMILY -> ChagokTheme.colors.relation.family
    }
