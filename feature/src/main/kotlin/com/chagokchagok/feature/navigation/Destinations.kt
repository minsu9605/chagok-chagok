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
 * @property guided 갈 곳이 정해진 채로 시작했으면 true (화면 08-1)
 */
@Serializable
data class DriveDestination(
    val justStarted: Boolean = false,
    val autoStarted: Boolean = false,
    val guided: Boolean = false,
)

/**
 * 일기 완성 화면 (화면 29).
 *
 * @property sessionId 방금 끝낸 기록의 id
 */
@Serializable
data class DiaryCompleteDestination(val sessionId: String)

/** 일기 보기 (화면 44~47). */
@Serializable
data object DiaryListDestination

/**
 * 일기 상세 (화면 48~55).
 *
 * @property sessionId 볼 일기의 id
 */
@Serializable
data class DiaryDetailDestination(val sessionId: String)
