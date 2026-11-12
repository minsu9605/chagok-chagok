import com.android.build.api.dsl.ApplicationExtension
import com.chagokchagok.buildlogic.ProjectConfig
import com.chagokchagok.buildlogic.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.android")

            configureKotlinAndroid()
            extensions.configure<ApplicationExtension> {
                defaultConfig.targetSdk = ProjectConfig.TARGET_SDK
            }
        }
    }
}
