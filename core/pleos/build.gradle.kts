plugins {
    alias(libs.plugins.chagok.android.library)
    alias(libs.plugins.chagok.android.hilt)
}

android {
    namespace = "com.chagokchagok.core.pleos"
    // 주행 중 UX 제한(CarUxRestrictionsManager)을 받는 AAOS 차량 API. 기기에는 시스템에 들어 있다.
    useLibrary("android.car")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.core)

    // Pleos Connect SDK
    implementation(libs.pleos.vehicle)
    implementation(libs.pleos.navihelper)
    implementation(libs.pleos.speechtotext)
    implementation(libs.pleos.texttospeech)
}
