package com.kaminga.reader.data.repository

import com.kaminga.reader.data.demo.DemoData
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.repository.ComicNotFoundException
import com.kaminga.reader.domain.repository.ComicRepository
import kotlinx.coroutines.delay

/**
 * مخزن نمایشی: فقط داده‌ی ساختگی با برچسب Demo برمی‌گرداند و به شبکه وصل نیست.
 * تأخیر کوتاه برای دیدن حالت «در حال بارگذاری» است.
 */
class DemoComicRepository(private val latencyMillis: Long = 350L) : ComicRepository {

    override suspend fun getComics(): Result<List<Comic>> {
        simulateLatency()
        return Result.success(DemoData.comics)
    }

    override suspend fun getComic(id: String): Result<Comic> {
        simulateLatency()
        val comic = DemoData.comics.firstOrNull { it.id == id }
        return if (comic != null) Result.success(comic) else Result.failure(ComicNotFoundException(id))
    }

    private suspend fun simulateLatency() {
        if (latencyMillis > 0) delay(latencyMillis)
    }
}
