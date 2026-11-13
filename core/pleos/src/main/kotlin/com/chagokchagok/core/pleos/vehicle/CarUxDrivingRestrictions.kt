package com.chagokchagok.core.pleos.vehicle

import android.car.Car
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.content.Context
import android.util.Log
import com.chagokchagok.core.domain.vehicle.DrivingRestrictions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AAOS `CarUxRestrictionsManager`로 주행 중 UX 제한을 받는다 (Pleos 가이드 "주행 규제", "지역별 주행 규제 정책").
 * 앱이 주행 중 사용(distractionOptimized)을 선언했으므로, 키보드 금지 같은 기능별 제한은 앱이 직접 지켜야 한다.
 */
@Singleton
class CarUxDrivingRestrictions @Inject constructor(
    @ApplicationContext private val context: Context,
) : DrivingRestrictions {

    override val keyboardBlocked: Flow<Boolean> = callbackFlow {
        val car: Car
        val manager: CarUxRestrictionsManager
        try {
            car = Car.createCar(context)
            manager = car.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as CarUxRestrictionsManager
        } catch (e: Exception) {
            // 제한 상태를 알 수 없으면 안전한 쪽(키보드 막음)을 택한다. 음성 입력은 그대로 쓸 수 있다.
            Log.w(TAG, "car ux restrictions unavailable", e)
            trySend(true)
            awaitClose()
            return@callbackFlow
        }

        manager.registerListener { restrictions -> trySend(restrictions.blocksKeyboard()) }
        trySend(manager.currentCarUxRestrictions.blocksKeyboard())

        awaitClose {
            try {
                manager.unregisterListener()
                car.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "car ux restrictions release failed", e)
            }
        }
    }.distinctUntilChanged()

    private fun CarUxRestrictions?.blocksKeyboard(): Boolean {
        if (this == null) return true
        return isRequiresDistractionOptimization &&
            (activeRestrictions and CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD) != 0
    }

    private companion object {
        const val TAG = "CarUxRestrictions"
    }
}
