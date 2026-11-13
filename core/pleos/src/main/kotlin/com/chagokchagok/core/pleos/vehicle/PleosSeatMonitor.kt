package com.chagokchagok.core.pleos.vehicle

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.constant.area.SeatValue
import ai.pleos.playground.vehicle.listener.SeatOccupancyListener
import android.content.Context
import android.util.Log
import com.chagokchagok.core.domain.vehicle.SeatMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Vehicle SDK의 좌석 착석 값으로 탑승 인원을 센다.
 *
 * - 좌석마다 착석 여부를 구독하고, 앉아 있는 좌석 수를 낸다.
 * - 차량이 지원하지 않는 좌석(예: 3열)은 구독이 실패하므로 세지 않는다.
 * - 값이 없는(null) 좌석은 비어 있는 것으로 본다.
 */
@Singleton
class PleosSeatMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) : SeatMonitor {

    override val occupantCount: Flow<Int> = callbackFlow {
        val vehicle = Vehicle(context)
        val occupiedBySeat = ConcurrentHashMap<SeatValue, Boolean>()
        val listeners = mutableMapOf<SeatArea, SeatOccupancyListener>()

        vehicle.initialize()
        SeatValue.entries.forEach { seatValue ->
            val area = SeatArea(seatValue)
            val listener = object : SeatOccupancyListener {
                override fun onSeatOccupancyUpdated(occupancy: Boolean?) {
                    occupiedBySeat[seatValue] = occupancy == true
                    trySend(occupiedBySeat.values.count { it })
                }

                override fun onFailed(e: Exception) {
                    // 이 차량에 없는 좌석이다. 세지 않는다.
                    Log.d(TAG, "seat $seatValue not available: ${e.message}")
                }
            }
            try {
                vehicle.getSeat().registerSeatOccupancy(area, listener)
                listeners[area] = listener
            } catch (e: Exception) {
                Log.w(TAG, "seat $seatValue register failed", e)
            }
        }

        awaitClose {
            listeners.forEach { (area, listener) ->
                try {
                    vehicle.getSeat().unregisterSeatOccupancy(area, listener)
                } catch (e: Exception) {
                    Log.w(TAG, "seat unregister failed", e)
                }
            }
            vehicle.release()
        }
    }.conflate().distinctUntilChanged()

    private companion object {
        const val TAG = "PleosSeatMonitor"
    }
}
