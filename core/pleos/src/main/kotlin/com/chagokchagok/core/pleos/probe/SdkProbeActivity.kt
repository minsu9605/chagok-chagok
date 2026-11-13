package com.chagokchagok.core.pleos.probe

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.constants.NavigationStatus
import ai.pleos.playground.navi.data.CurrentLocationInfo
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.DestinationInfo
import ai.pleos.playground.navi.data.NavigationScreenRequest
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.RouteStartInfo
import ai.pleos.playground.navi.data.RouteStateInfo
import ai.pleos.playground.navi.data.WaypointArrivedInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.constant.area.SeatValue
import ai.pleos.playground.vehicle.listener.SeatOccupancyListener
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.ScrollView
import android.widget.TextView

/**
 * [확인용] 에뮬레이터에서 Pleos SDK가 실제로 동작하는지 보는 화면. 확인이 끝나면 삭제한다.
 *
 * 켜기: `adb shell am start -n com.chagokchagok/com.chagokchagok.core.pleos.probe.SdkProbeActivity`
 * 명령: 위 명령 뒤에 `--es cmd <이름>`을 붙인다.
 *   loc(현재 위치), dest(목적지 정보), state(경로 상태), route(목적지 설정), cancel(경로 취소),
 *   screen(내비 화면 요청), seat(좌석 착석 다시 읽기)
 * 결과는 화면과 logcat(태그 ChagokProbe)에 함께 적는다.
 */
class SdkProbeActivity : Activity() {
    private lateinit var logView: TextView
    private lateinit var naviHelper: NaviHelper
    private lateinit var vehicle: Vehicle

    private val naviListener = object : NaviHelperEventListener {
        override fun onRouteStarted(routeStartInfo: RouteStartInfo) = log("navi onRouteStarted $routeStartInfo")
        override fun onRouteEnded() = log("navi onRouteEnded")
        override fun onRoutingCompleted() = log("navi onRoutingCompleted")
        override fun onRouteCancelled() = log("navi onRouteCancelled")
        override fun onDestinationChanged() = log("navi onDestinationChanged")
        override fun onDestinationArrived(destinationArrivedInfo: DestinationArrivedInfo) =
            log("navi onDestinationArrived $destinationArrivedInfo")
        override fun onWayPointArrived(waypointArrivedInfo: WaypointArrivedInfo) =
            log("navi onWayPointArrived $waypointArrivedInfo")
        override fun onError(errorCode: NaviErrorCode) = log("navi onError $errorCode")
        override fun onCurrentLocationInfo(currentLocationInfo: CurrentLocationInfo) =
            log("navi onCurrentLocationInfo $currentLocationInfo")
        override fun onRouteStateInfo(routeStateInfo: RouteStateInfo) = log("navi onRouteStateInfo $routeStateInfo")
        override fun onDestinationInfo(destinationInfo: DestinationInfo) = log("navi onDestinationInfo $destinationInfo")
        override fun onNavigationStatus(status: NavigationStatus) = log("navi onNavigationStatus $status")
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = log("broadcast ${intent.action}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logView = TextView(this).apply { textSize = LOG_TEXT_SIZE_SP }
        setContentView(ScrollView(this).apply { addView(logView) })

        naviHelper = NaviHelper(this)
        naviHelper.addListener(naviListener)
        naviHelper.initialize(
            onConnected = { log("navi connected") },
            onFailed = { log("navi connect FAILED $it") },
        )

        vehicle = Vehicle(this)
        vehicle.initialize()
        readSeats(register = true)

        registerReceiver(
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            },
        )
        handleCommand(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleCommand(intent)
    }

    override fun onDestroy() {
        unregisterReceiver(screenReceiver)
        naviHelper.removeListener(naviListener)
        naviHelper.release()
        vehicle.release()
        super.onDestroy()
    }

    private fun handleCommand(intent: Intent?) {
        val command = intent?.getStringExtra(EXTRA_COMMAND) ?: return
        log("cmd $command")
        try {
            when (command) {
                "loc" -> naviHelper.getCurrentLocationInfo()
                "dest" -> naviHelper.getDestinationInfo()
                "state" -> naviHelper.getRouteStateInfo()
                // 좌표와 장소 정보는 NaviHelper 가이드 문서의 예시 값이다.
                "route" -> naviHelper.requestRoute(
                    RouteInfo(
                        latitude = 37.48544722,
                        longitude = 127.03636666,
                        poiName = "42dot",
                        poiId = "36khp37hh8gqcm5nqhe5",
                        address = "서울시 강남구 남부순환로 2621",
                        poiSubId = "0",
                    ),
                )
                "cancel" -> naviHelper.cancelRoute()
                "screen" -> naviHelper.requestNavigationScreen(NavigationScreenRequest.Foreground)
                "seat" -> readSeats(register = false)
                "stt" -> startStt(fromFile = false)
                "sttfile" -> startStt(fromFile = true)
                "tts" -> startTts()
                else -> log("모르는 명령: $command")
            }
        } catch (e: Exception) {
            log("cmd $command THREW $e")
        }
    }

    private var speechToText: ai.pleos.playground.stt.SpeechToText? = null
    private var textToSpeech: ai.pleos.playground.tts.TextToSpeech? = null

    /**
     * 차 안 엔진(ON_DEVICE)으로 한국어 음성 인식을 한 번 요청한다.
     *
     * @param fromFile true면 마이크 대신 앱 파일 폴더의 `stt-test.pcm`(16kHz, 16bit, mono)을 넣는다
     */
    private fun startStt(fromFile: Boolean) {
        val stt = ai.pleos.playground.stt.SpeechToText(applicationContext, ai.pleos.playground.stt.constant.Mode.ON_DEVICE)
        speechToText = stt
        stt.addListener(
            object : ai.pleos.playground.stt.listener.ResultListener {
                override fun onReady() = log("stt onReady")
                override fun onStartedRecognition() = log("stt onStartedRecognition")
                override fun onUpdated(stt: String, completed: Boolean) = log("stt onUpdated '$stt' completed=$completed")
                override fun onUpdatedEpdData(on: Long, off: Long) = log("stt epd on=$on off=$off")
                override fun onUpdatedRms(rms: Double) = Unit
                override fun onEndedRecognition() = log("stt onEndedRecognition")
                override fun onError(errorType: ai.pleos.playground.stt.constant.ErrorType) = log("stt onError $errorType")
            },
        )
        stt.initialize(
            object : ai.pleos.playground.stt.listener.OnInitializeListener {
                override fun onSuccess() {
                    log("stt init success")
                    stt.setLocale(java.util.Locale.KOREA)
                    if (fromFile) {
                        val pcm = java.io.File(filesDir, "stt-test.pcm").readBytes()
                        // 가이드 문서의 예시대로 100ms 분량(3,200바이트)씩 실시간 속도로 보낸다.
                        Thread {
                            Thread.sleep(1_000)
                            pcm.asList().chunked(3_200).forEach { chunk ->
                                stt.sendAudio(chunk.toByteArray())
                                Thread.sleep(100)
                            }
                            stt.completeAudioSend()
                            log("stt sent ${pcm.size} bytes from file")
                        }.start()
                    } else {
                        stt.request()
                        log("stt request sent")
                    }
                }

                override fun onFailure(errorType: ai.pleos.playground.stt.constant.ErrorType) = log("stt init FAILED $errorType")
            },
        )
    }

    /** 차 안 엔진(ON_DEVICE)으로 한국어 문장을 읽게 한다. */
    private fun startTts() {
        val tts = ai.pleos.playground.tts.TextToSpeech(applicationContext, ai.pleos.playground.tts.constant.Mode.ON_DEVICE)
        textToSpeech = tts
        tts.initialize(
            object : ai.pleos.playground.tts.listener.OnInitializeListener {
                override fun onSuccess() {
                    log("tts init success")
                tts.addEventListener(
                    object : ai.pleos.playground.tts.listener.EventListener {
                        override fun onReady() = log("tts onReady")
                        override fun onStart() = log("tts onStart")
                        override fun onUpdatedRms(rms: Double) = Unit
                        override fun onDone() = log("tts onDone")
                        override fun onStop() = log("tts onStop")
                        override fun onError(errorType: ai.pleos.playground.tts.constant.ErrorType) = log("tts onError $errorType")
                    },
                )
                    tts.setLocale(java.util.Locale.KOREA)
                    tts.speak("차곡차곡 기록이 저장되었어요.")
                    log("tts speak sent")
                }

                override fun onFailure(errorType: ai.pleos.playground.tts.constant.ErrorType) = log("tts init FAILED $errorType")
            },
        )
    }

    private fun readSeats(register: Boolean) {
        SeatValue.entries.forEach { seatValue ->
            val area = SeatArea(seatValue)
            try {
                vehicle.getSeat().getCurrentSeatOccupancy(
                    area = area,
                    onComplete = { occupied -> log("seat $seatValue occupied=$occupied") },
                    onFailed = { log("seat $seatValue FAILED $it") },
                )
                if (register) {
                    vehicle.getSeat().registerSeatOccupancy(
                        area,
                        object : SeatOccupancyListener {
                            override fun onSeatOccupancyUpdated(occupancy: Boolean?) =
                                log("seat $seatValue UPDATED occupied=$occupancy")

                            override fun onFailed(e: Exception) = log("seat $seatValue listener FAILED $e")
                        },
                    )
                }
            } catch (e: Exception) {
                log("seat $seatValue THREW $e")
            }
        }
    }

    private fun log(message: String) {
        Log.i(TAG, message)
        runOnUiThread { logView.append(message + "\n") }
    }

    private companion object {
        const val TAG = "ChagokProbe"
        const val EXTRA_COMMAND = "cmd"
        const val LOG_TEXT_SIZE_SP = 16f
    }
}
