package com.chagokchagok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.chagokchagok.core.designsystem.theme.ChagokTheme
import com.chagokchagok.navigation.AppNavHost
import com.chagokchagok.ui.ProvideDesignDensity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProvideDesignDensity {
                ChagokTheme {
                    AppNavHost()
                }
            }
        }
    }
}
