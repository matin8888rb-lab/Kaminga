package com.kaminga.reader.domain

import com.kaminga.reader.domain.model.Chapter
import com.kaminga.reader.domain.model.ComicType
import com.kaminga.reader.domain.model.ImportedChapter
import com.kaminga.reader.domain.model.ReaderMode
import com.kaminga.reader.domain.model.ReadingDirection
import com.kaminga.reader.domain.model.ReadingProgress
import com.kaminga.reader.domain.model.defaultReaderMode
import com.kaminga.reader.domain.model.defaultReadingDirection
import com.kaminga.reader.domain.usecase.buildContinueList
import com.kaminga.reader.domain.usecase.describe
import com.kaminga.reader.domain.usecase.naturalCompare
import com.kaminga.reader.domain.usecase.withImportedChapters
import com.kaminga.reader.testutil.sampleComic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryLogicTest {

    private val comic = sampleComic("a").copy(
        chapters = listOf(
            Chapter("a-1", 1, "یک", pageCount = 4),
            Chapter("a-2", 2, "دو", pageCount = 4),
            Chapter("a-3", 3, "سه", pageCount = 4)
        )
    )

    @Test
    fun importedChaptersAreNumberedAfterExistingOnesInCreationOrder() {
        val imported = listOf(
            ImportedChapter("late", "a", "دوم", listOf("x", "y", "z"), createdAt = 20L),
            ImportedChapter("early", "a", "اول", listOf("p"), createdAt = 10L),
            ImportedChapter("other", "b", "دیگری", listOf("q"), createdAt = 5L)
        )
        val merged = comic.withImportedChapters(imported)

        assertEquals(listOf("a-1", "a-2", "a-3", "early", "late"), merged.chapters.map { it.id })
        assertEquals(listOf(4, 5), merged.chapters.takeLast(2).map { it.number })
        assertTrue(merged.chapters.takeLast(2).all { it.isPersonal })
        assertEquals(3, merged.chapters.last().pageCount)
        assertFalse(merged.chapters.first().isPersonal)
    }

    @Test
    fun comicWithoutImportedChaptersIsReturnedUnchanged() {
        val imported = listOf(ImportedChapter("other", "b", "x", listOf("q"), 1L))
        assertSame(comic, comic.withImportedChapters(imported))
    }

    @Test
    fun importedChaptersStartAtOneWhenComicHasNoChapters() {
        val empty = sampleComic("e")
        val merged = empty.withImportedChapters(listOf(ImportedChapter("i", "e", "t", listOf("p"), 1L)))
        assertEquals(1, merged.chapters.single().number)
    }

    @Test
    fun continueListIsSortedByRecencyAndSkipsUnknownComics() {
        val other = sampleComic("b")
        val progress = mapOf(
            "a" to ReadingProgress("a", "a-1", 1, 0, 4, updatedAt = 10L),
            "b" to ReadingProgress("b", "b-1", 1, 0, 4, updatedAt = 30L),
            "gone" to ReadingProgress("gone", "g-1", 1, 0, 4, updatedAt = 99L)
        )
        val list = buildContinueList(listOf(comic, other), progress)
        assertEquals(listOf("b", "a"), list.map { it.comic.id })
    }

    @Test
    fun progressDescriptionIsHumanReadable() {
        val progress = ReadingProgress("a", "a-3", chapterNumber = 3, pageIndex = 2, pageCount = 10, updatedAt = 1L)
        assertEquals("فصل 3 • صفحه 3 از 10", progress.describe())
    }

    @Test
    fun naturalCompareOrdersNumbersNumerically() {
        val sorted = listOf("10.jpg", "2.jpg", "1.jpg", "b.jpg", "A.jpg")
            .sortedWith(Comparator { a, b -> naturalCompare(a, b) })
        assertEquals(listOf("1.jpg", "2.jpg", "10.jpg", "A.jpg", "b.jpg"), sorted)
        assertTrue(naturalCompare("page9", "page10") < 0)
        assertTrue(naturalCompare("page", "page1") < 0)
        assertEquals(0, naturalCompare("same", "same"))
    }

    @Test
    fun readerDefaultsDependOnComicType() {
        assertEquals(ReaderMode.VERTICAL, ComicType.WEBTOON.defaultReaderMode())
        assertEquals(ReaderMode.VERTICAL, ComicType.MANHWA.defaultReaderMode())
        assertEquals(ReaderMode.PAGED, ComicType.COMIC.defaultReaderMode())
        assertEquals(ReaderMode.PAGED, ComicType.MANGA.defaultReaderMode())
        assertEquals(ReadingDirection.RTL, ComicType.MANGA.defaultReadingDirection())
        assertEquals(ReadingDirection.LTR, ComicType.COMIC.defaultReadingDirection())
        assertEquals(ReadingDirection.LTR, ComicType.WEBTOON.defaultReadingDirection())
    }
}
