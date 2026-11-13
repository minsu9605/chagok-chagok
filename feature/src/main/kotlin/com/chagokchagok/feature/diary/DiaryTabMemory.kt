package com.chagokchagok.feature.diary

import com.chagokchagok.feature.common.RelationUi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 일기장에서 마지막으로 보던 관계 탭. 홈에 다녀와도 그 탭으로 다시 열린다.
 * 앱을 새로 켜면 전체부터다. 그래서 저장하지 않고 메모리에만 둔다.
 */
@Singleton
class DiaryTabMemory @Inject constructor() {
    /** 보던 탭의 관계. 전체 탭이면 null */
    var tab: RelationUi? = null
}
