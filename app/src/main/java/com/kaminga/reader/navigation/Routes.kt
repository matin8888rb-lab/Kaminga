package com.kaminga.reader.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val LIBRARY = "library"

    const val COMIC_ID_ARG = "comicId"
    const val DETAILS = "details/{$COMIC_ID_ARG}"

    const val CHAPTER_ID_ARG = "chapterId"
    const val READER = "reader/{$COMIC_ID_ARG}/{$CHAPTER_ID_ARG}"

    fun details(comicId: String): String = "details/$comicId"

    fun reader(comicId: String, chapterId: String): String = "reader/$comicId/$chapterId"
}

enum class TopLevelDestination(val route: String, val label: String, val icon: ImageVector) {
    HOME(Routes.HOME, "خانه", Icons.Default.Home),
    SEARCH(Routes.SEARCH, "کشف", Icons.Default.Search),
    LIBRARY(Routes.LIBRARY, "کتابخانه‌ی من", Icons.Default.Favorite);

    companion object {
        val routes: Set<String> = entries.map { it.route }.toSet()
    }
}
