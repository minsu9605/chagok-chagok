package com.chagokchagok.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chagokchagok.feature.navigation.HomeDestination
import com.chagokchagok.feature.record.home.HomeScreen

/** 화면 연결 지점. 목적지는 feature 모듈이 선언하고, 여기서 등록한다. */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination,
        modifier = modifier,
    ) {
        composable<HomeDestination> {
            HomeScreen()
        }
    }
}
