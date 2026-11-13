package com.chagokchagok.core.pleos.voice

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.OnInitializeListener
import android.content.Context
import android.util.Log
import com.chagokchagok.core.domain.voice.VoiceSpeaker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TextToSpeech SDK의 차 안 엔진(ON_DEVICE)으로 한국어 문장을 읽는다.
 * ON_DEVICE 모드는 인터넷과 앱 등록(클라이언트 ID·시크릿)이 필요 없다.
 *
 * 확인 상태(2026-11-13, 에뮬레이터): 읽기 시작(onStart)과 끝(onDone) 이벤트가 오는 것을 확인했다.
 * 스피커로 실제 소리가 나는지는 확인하지 못했다.
 */
@Singleton
class PleosVoiceSpeaker @Inject constructor(
    @ApplicationContext private val context: Context,
) : VoiceSpeaker {
    private val textToSpeech by lazy { TextToSpeech(context, Mode.ON_DEVICE) }
    private val lock = Any()
    private var state = State.IDLE

    /** 초기화가 끝나기 전에 들어온 문장. 마지막 것만 읽는다. */
    private var waitingText: String? = null

    override fun speak(text: String) {
        synchronized(lock) {
            when (state) {
                State.READY -> speakNow(text)
                State.INITIALIZING -> waitingText = text
                State.IDLE -> {
                    waitingText = text
                    state = State.INITIALIZING
                    initialize()
                }
            }
        }
    }

    private fun initialize() {
        textToSpeech.initialize(
            object : OnInitializeListener {
                override fun onSuccess() {
                    synchronized(lock) {
                        textToSpeech.setLocale(Locale.KOREA)
                        state = State.READY
                        waitingText?.let(::speakNow)
                        waitingText = null
                    }
                }

                override fun onFailure(errorType: ErrorType) {
                    Log.w(TAG, "tts init failed $errorType")
                    synchronized(lock) {
                        state = State.IDLE
                        waitingText = null
                    }
                }
            },
        )
    }

    private fun speakNow(text: String) {
        try {
            textToSpeech.speak(text)
        } catch (e: Exception) {
            Log.w(TAG, "tts speak failed", e)
        }
    }

    private enum class State { IDLE, INITIALIZING, READY }

    private companion object {
        const val TAG = "PleosVoiceSpeaker"
    }
}
