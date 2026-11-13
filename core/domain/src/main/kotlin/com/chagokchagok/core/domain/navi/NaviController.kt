package com.chagokchagok.core.domain.navi

/** 순정 내비에 요청을 보낸다. */
interface NaviController {
    /** 내비 화면을 앞으로 불러 달라고 요청한다. 내비가 응답하지 않아도 오류를 내지 않는다. */
    fun showNavigationScreen()
}
