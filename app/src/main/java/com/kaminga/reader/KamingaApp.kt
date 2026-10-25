package com.kaminga.reader

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kaminga.reader.feature.details.DetailsScreen
import com.kaminga.reader.feature.home.HomeScreen
import com.kaminga.reader.feature.library.LibraryScreen
import com.kaminga.reader.feature.reader.ReaderScreen
import com.kaminga.reader.feature.search.SearchScreen
import com.kaminga.reader.navigation.Routes
import com.kaminga.reader.navigation.TopLevelDestination
import com.kaminga.reader.ui.theme.Cyan
import com.kaminga.reader.ui.theme.KamingaTheme
import com.kaminga.reader.ui.theme.Muted
import com.kaminga.reader.ui.theme.Night
import com.kaminga.reader.ui.theme.Panel

@Composable
fun KamingaApp() {
    KamingaTheme {
        // نسخه‌ی اول فارسی‌محور است و چیدمان همیشه راست‌به‌چپ است.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val isReader = currentRoute == Routes.READER

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (currentRoute in TopLevelDestination.routes) {
                        NavigationBar(containerColor = Panel) {
                            TopLevelDestination.entries.forEach { destination ->
                                NavigationBarItem(
                                    selected = currentRoute == destination.route,
                                    onClick = { navController.navigateTopLevel(destination.route) },
                                    icon = { Icon(destination.icon, contentDescription = null) },
                                    label = { Text(destination.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Night,
                                        selectedTextColor = Cyan,
                                        indicatorColor = Cyan,
                                        unselectedIconColor = Muted,
                                        unselectedTextColor = Muted
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                // مطالعه‌گر تمام‌صفحه است و حاشیه‌ی نوارهای سیستم را خودش مدیریت می‌کند.
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = if (isReader) Modifier else Modifier.padding(innerPadding)
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            onComicClick = { id -> navController.navigate(Routes.details(id)) },
                            onContinueClick = { comicId, chapterId ->
                                navController.navigate(Routes.reader(comicId, chapterId))
                            },
                            onSearchClick = { navController.navigateTopLevel(Routes.SEARCH) }
                        )
                    }
                    composable(Routes.SEARCH) {
                        SearchScreen(onComicClick = { id -> navController.navigate(Routes.details(id)) })
                    }
                    composable(Routes.LIBRARY) {
                        LibraryScreen(
                            onComicClick = { id -> navController.navigate(Routes.details(id)) },
                            onContinueClick = { comicId, chapterId ->
                                navController.navigate(Routes.reader(comicId, chapterId))
                            }
                        )
                    }
                    composable(
                        route = Routes.DETAILS,
                        arguments = listOf(navArgument(Routes.COMIC_ID_ARG) { type = NavType.StringType })
                    ) { entry ->
                        val comicId = entry.arguments?.getString(Routes.COMIC_ID_ARG).orEmpty()
                        DetailsScreen(
                            comicId = comicId,
                            onBack = { navController.popBackStack() },
                            onOpenReader = { id, chapterId ->
                                navController.navigate(Routes.reader(id, chapterId))
                            }
                        )
                    }
                    composable(
                        route = Routes.READER,
                        arguments = listOf(
                            navArgument(Routes.COMIC_ID_ARG) { type = NavType.StringType },
                            navArgument(Routes.CHAPTER_ID_ARG) { type = NavType.StringType }
                        )
                    ) { entry ->
                        val comicId = entry.arguments?.getString(Routes.COMIC_ID_ARG).orEmpty()
                        val chapterId = entry.arguments?.getString(Routes.CHAPTER_ID_ARG).orEmpty()
                        ReaderScreen(
                            comicId = comicId,
                            chapterId = chapterId,
                            onBack = { navController.popBackStack() },
                            onOpenChapter = { nextChapterId ->
                                navController.navigate(Routes.reader(comicId, nextChapterId)) {
                                    popUpTo(Routes.READER) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
