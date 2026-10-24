package com.kaminga.reader.di

import com.kaminga.reader.data.repository.DemoComicRepository
import com.kaminga.reader.domain.repository.ComicRepository

/**
 * ساده‌ترین شکل تزریق وابستگی برای مرحله‌ی اول.
 * در مرحله‌ی بک‌اند، فقط این‌جا مخزن نمایشی با مخزن واقعی عوض می‌شود.
 */
object AppContainer {
    val comicRepository: ComicRepository by lazy { DemoComicRepository() }
}
