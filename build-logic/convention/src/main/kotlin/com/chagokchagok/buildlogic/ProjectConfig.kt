package com.chagokchagok.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/** SDK·JVM 버전은 이 파일 한 곳에서만 정한다. 근거: Pleos 샘플 앱 기준, minSdk 28은 Pleos 필수 요건. */
internal object ProjectConfig {
    const val COMPILE_SDK = 35
    const val TARGET_SDK = 35
    const val MIN_SDK = 28
    val javaVersion = JavaVersion.VERSION_17
    val jvmTarget = JvmTarget.JVM_17
}

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/** 안드로이드 모듈(application, library)의 `android` 확장. */
internal val Project.androidExtension: CommonExtension<*, *, *, *, *, *>
    get() = extensions.getByName("android") as CommonExtension<*, *, *, *, *, *>

internal fun Project.configureKotlinAndroid() {
    androidExtension.apply {
        compileSdk = ProjectConfig.COMPILE_SDK
        defaultConfig {
            minSdk = ProjectConfig.MIN_SDK
        }
        compileOptions {
            sourceCompatibility = ProjectConfig.javaVersion
            targetCompatibility = ProjectConfig.javaVersion
        }
    }
    configureKotlinJvmTarget()
}

internal fun Project.configureKotlinJvmTarget() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(ProjectConfig.jvmTarget)
        }
    }
}
