package com.chagokchagok.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.chagokchagok.feature.diary.DiaryDetailScreen
import com.chagokchagok.feature.diary.DiaryListScreen
import com.chagokchagok.feature.navigation.DiaryCompleteDestination
import com.chagokchagok.feature.navigation.DiaryDetailDestination
import com.chagokchagok.feature.navigation.DiaryListDestination
import com.chagokchagok.feature.navigation.DriveDestination
import com.chagokchagok.feature.navigation.HomeDestination
import com.chagokchagok.feature.record.complete.DiaryCompleteScreen
import com.chagokchagok.feature.record.drive.DriveScreen
import com.chagokchagok.feature.record.home.HomeScreen
import com.chagokchagok.feature.record.memory.MemoryPromptHost

/** 화면 연결 지점. 목적지는 feature 모듈이 선언하고, 여기서 등록한다. */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    // 기록을 끝내면 일기 완성 화면으로 간다. 닫으면 홈으로 돌아오도록 홈만 남긴다.
    val showDiaryComplete: (String) -> Unit = { sessionId ->
        navController.navigate(DiaryCompleteDestination(sessionId)) {
            popUpTo<HomeDestination>()
            launchSingleTop = true
        }
    }

    // 기록을 시작하면 주행 화면으로 간다. 주행 화면에서 나오면 홈이 보이도록 홈만 남긴다.
    val showDrive: (DriveDestination) -> Unit = { destination ->
        navController.navigate(destination) {
            popUpTo<HomeDestination>()
            launchSingleTop = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = HomeDestination,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable<HomeDestination> {
                HomeScreen(
                    onRecordEnded = { sessionId -> showDiaryComplete(sessionId) },
                    onGoToNaviClick = { navController.navigate(DriveDestination()) { launchSingleTop = true } },
                    onRecordStarted = { autoStarted, guided ->
                        showDrive(
                            DriveDestination(justStarted = true, autoStarted = autoStarted, guided = guided),
                        )
                    },
                    onDiaryClick = { navController.navigate(DiaryListDestination) { launchSingleTop = true } },
                )
            }
            composable<DriveDestination> { backStackEntry ->
                val destination = backStackEntry.toRoute<DriveDestination>()
                val backToHome = { navController.popBackStack(route = HomeDestination, inclusive = false) }
                DriveScreen(
                    justStarted = destination.justStarted,
                    autoStarted = destination.autoStarted,
                    guided = destination.guided,
                    onHomeClick = { backToHome() },
                    onRecordEnded = { sessionId -> showDiaryComplete(sessionId) },
                    onNotRecording = { backToHome() },
                )
            }
            composable<DiaryCompleteDestination> {
                DiaryCompleteScreen(
                    onCloseClick = { navController.popBackStack(route = HomeDestination, inclusive = false) },
                )
            }
            composable<DiaryListDestination> {
                DiaryListScreen(
                    onHomeClick = { navController.popBackStack(route = HomeDestination, inclusive = false) },
                    onDiaryClick = { sessionId ->
                        navController.navigate(DiaryDetailDestination(sessionId)) { launchSingleTop = true }
                    },
                    onRecordStarted = { autoStarted ->
                        showDrive(DriveDestination(justStarted = true, autoStarted = autoStarted))
                    },
                )
            }
            composable<DiaryDetailDestination> {
                DiaryDetailScreen(
                    onBackClick = { navController.popBackStack(route = DiaryListDestination, inclusive = false) },
                    onDeparted = { newRecord, autoStarted ->
                        // 갈 곳이 정해진 채 시작한다. 하던 기록에 이어서 쌓을 때는 시작 안내를 띄우지 않는다.
                        showDrive(
                            DriveDestination(justStarted = newRecord, autoStarted = autoStarted, guided = newRecord),
                        )
                    },
                )
            }
        }
        // 기억 남기기 팝업은 어느 화면에 있든 그 위에 뜬다.
        MemoryPromptHost()
    }
}
