plugins {
    alias(libs.plugins.chagok.android.application)
    alias(libs.plugins.chagok.android.compose)
    alias(libs.plugins.chagok.android.hilt)
}

android {
    namespace = "com.chagokchagok"

    defaultConfig {
        applicationId = "com.chagokchagok"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":feature"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))

    // 화면은 아래 모듈을 직접 참조하지 않는다. Hilt 바인딩을 조립하기 위해 app만 의존한다.
    implementation(project(":core:database"))
    implementation(project(":core:pleos"))
    implementation(project(":core:remote"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.compose)
}
