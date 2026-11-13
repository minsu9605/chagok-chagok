package com.chagokchagok.core.pleos.voice

import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.constant.ErrorType
import ai.pleos.playground.stt.constant.Mode
import ai.pleos.playground.stt.listener.OnInitializeListener
import ai.pleos.playground.stt.listener.ResultListener
import android.content.Context
import android.util.Log
import com.chagokchagok.core.domain.voice.VoiceEvent
import com.chagokchagok.core.domain.voice.VoiceRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SpeechToText SDK의 차 안 엔진(ON_DEVICE)으로 한국어를 받아 적는다.
 * ON_DEVICE 모드는 인터넷과 앱 등록(클라이언트 ID·시크릿)이 필요 없다.
 *
 * 확인 상태(2026-11-13, 에뮬레이터): 소리 파일을 넣었을 때 한국어 문장을 정확히 받아 적는 것을 확인했다.
 * 마이크 입력은 준비(onReady)까지만 확인했고, 실제 말소리가 인식되는지는 확인하지 못했다.
 */
@Singleton
class PleosVoiceRecognizer @Inject constructor(
    @ApplicationContext private val context: Context,
) : VoiceRecognizer {

    override fun listen(): Flow<VoiceEvent> = callbackFlow {
        val speechToText = SpeechToText(context, Mode.ON_DEVICE)
        val listener = object : ResultListener {
            override fun onReady() = Unit
            override fun onStartedRecognition() = Unit

            override fun onUpdated(stt: String, completed: Boolean) {
                trySend(VoiceEvent.Heard(text = stt, completed = completed))
                if (completed) close()
            }

            override fun onUpdatedEpdData(on: Long, off: Long) = Unit
            override fun onUpdatedRms(rms: Double) = Unit

            override fun onEndedRecognition() {
                // 최종 글은 이 호출 뒤에 올 수 있다. 잠시 기다려도 오지 않으면 끝낸다.
                launch {
                    delay(FINAL_RESULT_WAIT_MILLIS)
                    close()
                }
            }

            override fun onError(errorType: ErrorType) {
                Log.w(TAG, "stt error $errorType")
                trySend(VoiceEvent.Failed)
                close()
            }
        }
        speechToText.addListener(listener)
        speechToText.initialize(
            object : OnInitializeListener {
                override fun onSuccess() {
                    speechToText.setLocale(Locale.KOREA)
                    speechToText.request()
                }

                override fun onFailure(errorType: ErrorType) {
                    Log.w(TAG, "stt init failed $errorType")
                    trySend(VoiceEvent.Failed)
                    close()
                }
            },
        )

        awaitClose {
            try {
                speechToText.stop()
                speechToText.removeListener(listener)
                speechToText.release()
            } catch (e: Exception) {
                Log.w(TAG, "stt release failed", e)
            }
        }
    }

    private companion object {
        const val TAG = "PleosVoiceRecognizer"
        const val FINAL_RESULT_WAIT_MILLIS = 1_500L
    }
}
