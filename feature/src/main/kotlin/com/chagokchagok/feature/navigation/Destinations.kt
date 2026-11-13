package com.chagokchagok.feature.navigation

import kotlinx.serialization.Serializable

/** 메인 홈 화면. */
@Serializable
data object HomeDestination

/**
 * 주행 중 화면 (화면 11).
 *
 * @property justStarted 방금 기록을 시작해서 왔으면 true. 시작 안내 토스트를 띄운다 (화면 08, 09)
 * @property autoStarted 좌석 1명이라 묻지 않고 시작했으면 true
 */
@Serializable
data class DriveDestination(
    val justStarted: Boolean = false,
    val autoStarted: Boolean = false,
)

/**
 * 일기 완성 화면 (화면 29).
 *
 * @property sessionId 방금 끝낸 기록의 id
 */
@Serializable
data class DiaryCompleteDestination(val sessionId: String)
