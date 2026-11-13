package com.chagokchagok.core.domain.voice

import kotlinx.coroutines.flow.Flow

/** 차량 마이크로 말을 받아 적는다. */
interface VoiceRecognizer {
    /**
     * 한 번 듣는다. 모으기 시작하면 듣기 시작하고, 모으기를 그만두면 듣기를 멈춘다.
     * 받아 적은 글이 바뀔 때마다 [VoiceEvent.Heard]를 내고, 끝나면 흐름도 끝난다.
     */
    fun listen(): Flow<VoiceEvent>
}

sealed interface VoiceEvent {
    /**
     * 받아 적은 글.
     *
     * @property completed 말이 끝나 이 글이 최종이면 true
     */
    data class Heard(val text: String, val completed: Boolean) : VoiceEvent

    /** 듣지 못했다 (엔진 오류, 권한 없음 등). */
    data object Failed : VoiceEvent
}

/** 문장을 차 스피커로 읽어 준다. */
interface VoiceSpeaker {
    /** 읽어 달라고 요청한다. 읽지 못해도 오류를 내지 않는다. */
    fun speak(text: String)
}
