package com.chagokchagok.core.pleos.vehicle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.chagokchagok.core.domain.vehicle.IgnitionMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 화면 켜짐 방송(`ACTION_SCREEN_ON`)을 "시동을 다시 켰다"로 본다.
 * 시동 이벤트를 직접 주는 API가 없어서 쓰는 방법이다 (구현 분석 개요 2장, DB 설계 3장).
 *
 * 확인 상태(2026-11-13, 에뮬레이터): 화면을 껐다 켜면 방송이 오는 것을 확인했다. 실차 동작은 확인하지 못했다.
 */
@Singleton
class ScreenOnIgnitionMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) : IgnitionMonitor {

    override val restarted: Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(Unit)
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_SCREEN_ON))
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
