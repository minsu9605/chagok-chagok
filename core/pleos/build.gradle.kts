plugins {
    alias(libs.plugins.chagok.android.library)
    alias(libs.plugins.chagok.android.hilt)
}

android {
    namespace = "com.chagokchagok.core.pleos"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.core)

    // Pleos Connect SDK. 호출 코드와 권한 선언은 연동 단계에서 추가한다.
    implementation(libs.pleos.vehicle)
    implementation(libs.pleos.navihelper)
    implementation(libs.pleos.speechtotext)
    implementation(libs.pleos.texttospeech)
}
