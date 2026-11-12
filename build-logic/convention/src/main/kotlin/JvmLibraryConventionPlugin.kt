import com.chagokchagok.buildlogic.ProjectConfig
import com.chagokchagok.buildlogic.configureKotlinJvmTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure

/** Android 의존성이 없는 순수 Kotlin 모듈(core:domain)용. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = ProjectConfig.javaVersion
                targetCompatibility = ProjectConfig.javaVersion
            }
            configureKotlinJvmTarget()
        }
    }
}
