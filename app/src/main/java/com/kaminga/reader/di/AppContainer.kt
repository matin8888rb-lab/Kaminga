package com.kaminga.reader.di

import android.content.Context
import com.kaminga.reader.data.local.DataStoreUserLibraryRepository
import com.kaminga.reader.data.local.FileChapterImporter
import com.kaminga.reader.data.repository.DemoComicRepository
import com.kaminga.reader.domain.repository.ChapterImporter
import com.kaminga.reader.domain.repository.ComicRepository
import com.kaminga.reader.domain.repository.UserLibraryRepository

/**
 * ساده‌ترین شکل تزریق وابستگی. در مرحله‌ی بک‌اند فقط `comicRepository` عوض می‌شود.
 * `init` از `KamingaApplication.onCreate` صدا زده می‌شود.
 */
object AppContainer {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun requireContext(): Context =
        checkNotNull(appContext) { "AppContainer.init must be called from Application.onCreate" }

    val comicRepository: ComicRepository by lazy { DemoComicRepository() }
    val userLibrary: UserLibraryRepository by lazy { DataStoreUserLibraryRepository(requireContext()) }
    val chapterImporter: ChapterImporter by lazy { FileChapterImporter(requireContext()) }
}
