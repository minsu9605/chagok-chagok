package com.chagokchagok.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.chagokchagok.feature.navigation.DriveDestination
import com.chagokchagok.feature.navigation.HomeDestination
import com.chagokchagok.feature.record.drive.DriveScreen
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
            HomeScreen(
                onGoToNaviClick = { navController.navigate(DriveDestination()) { launchSingleTop = true } },
                onRecordStarted = { autoStarted ->
                    navController.navigate(DriveDestination(justStarted = true, autoStarted = autoStarted)) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<DriveDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<DriveDestination>()
            val backToHome = { navController.popBackStack(route = HomeDestination, inclusive = false) }
            DriveScreen(
                justStarted = destination.justStarted,
                autoStarted = destination.autoStarted,
                onHomeClick = { backToHome() },
                // 기록 종료와 일기 완성 화면은 다음 단계에서 연결한다.
                onEndRecordClick = {},
                onNotRecording = { backToHome() },
            )
        }
    }
}
