package com.chagokchagok.core.domain.vehicle

import kotlinx.coroutines.flow.Flow

/** 주행 중 화면 조작 제한. 무엇을 막을지는 차량이 지역 규정에 맞춰 내려 준다. */
interface DrivingRestrictions {
    /** 지금 키보드로 글자를 입력하면 안 되면 true (예: 주행 중). 모으기 시작하면 지금 값을 바로 낸다. */
    val keyboardBlocked: Flow<Boolean>
}
