plugins {
    alias(libs.plugins.chagok.android.library)
    alias(libs.plugins.chagok.android.hilt)
    alias(libs.plugins.chagok.android.room)
}

android {
    namespace = "com.chagokchagok.core.database"
}

dependencies {
    implementation(project(":core:domain"))
}
