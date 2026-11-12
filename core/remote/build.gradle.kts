plugins {
    alias(libs.plugins.chagok.android.library)
    alias(libs.plugins.chagok.android.hilt)
}

android {
    namespace = "com.chagokchagok.core.remote"
}

dependencies {
    implementation(project(":core:domain"))
}
