package com.chagokchagok.core.pleos.navi

import ai.pleos.playground.navi.data.NavigationScreenRequest
import ai.pleos.playground.navi.helper.NaviHelper
import android.content.Context
import android.util.Log
import com.chagokchagok.core.domain.navi.NaviController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NaviHelper SDK로 순정 내비에 요청을 보낸다.
 *
 * 확인 상태(2026-11-13, 에뮬레이터): SDK 연결과 요청 전달까지는 확인했다.
 * 내비 앱이 실제로 화면을 앞으로 내는지는 아직 확인하지 못했다(내비 앱이 준비되지 않은 상태였음).
 */
@Singleton
class PleosNaviController @Inject constructor(
    @ApplicationContext private val context: Context,
) : NaviController {
    private val naviHelper by lazy { NaviHelper(context) }

    @Volatile
    private var connected = false

    override fun showNavigationScreen() {
        if (connected) {
            requestForeground()
            return
        }
        naviHelper.initialize(
            onConnected = {
                connected = true
                requestForeground()
            },
            onFailed = { Log.w(TAG, "navi connect failed", it) },
        )
    }

    private fun requestForeground() {
        try {
            naviHelper.requestNavigationScreen(NavigationScreenRequest.Foreground)
        } catch (e: Exception) {
            Log.w(TAG, "requestNavigationScreen failed", e)
        }
    }

    private companion object {
        const val TAG = "PleosNaviController"
    }
}
