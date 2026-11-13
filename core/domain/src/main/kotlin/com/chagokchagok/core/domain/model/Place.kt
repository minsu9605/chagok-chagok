package com.chagokchagok.core.domain.model

/**
 * 장소. (DB 설계 2-3 `place` 중 1차에 쓰는 값만)
 * 좌표, 행정구역, 업종은 지도·카카오를 붙이는 2차에서 더한다.
 *
 * @property address 주소. 모르면 null
 */
data class Place(
    val id: String,
    val name: String,
    val address: String?,
)
