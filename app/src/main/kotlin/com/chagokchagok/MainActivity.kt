package com.chagokchagok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.core.domain.vehicle.DrivingRestrictions
import com.chagokchagok.feature.common.LocalKeyboardAllowed
import com.chagokchagok.navigation.AppNavHost
import com.chagokchagok.ui.ProvideDesignDensity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var drivingRestrictions: DrivingRestrictions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 주행 중 키보드 금지를 모든 화면에 내려 준다. 첫 값을 받기 전에는 막아 둔다.
            val keyboardBlocked by drivingRestrictions.keyboardBlocked.collectAsStateWithLifecycle(initialValue = true)
            CompositionLocalProvider(LocalKeyboardAllowed provides !keyboardBlocked) {
                ProvideDesignDensity {
                    ChagokTheme {
                        AppNavHost()
                    }
                }
            }
        }
    }
}
