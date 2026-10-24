package com.kaminga.reader.domain

import com.kaminga.reader.domain.model.ComicLanguage
import com.kaminga.reader.domain.model.ComicType
import com.kaminga.reader.domain.model.Genre
import com.kaminga.reader.domain.model.PublicationStatus
import com.kaminga.reader.domain.model.SearchFilters
import com.kaminga.reader.domain.model.SortOrder
import com.kaminga.reader.domain.usecase.filterAndSort
import com.kaminga.reader.domain.usecase.normalizeForSearch
import com.kaminga.reader.testutil.sampleComic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchComicsTest {

    private val a = sampleComic(
        id = "a", title = "کتاب ستاره", originalTitle = "STARFALL",
        type = ComicType.COMIC, genres = listOf(Genre.SCI_FI), author = "آرین",
        language = ComicLanguage.EN, status = PublicationStatus.ONGOING,
        popularity = 100, addedOrder = 3
    )
    private val b = sampleComic(
        id = "b", title = "ماجرای دریا", originalTitle = "SEA TALE",
        type = ComicType.MANGA, genres = listOf(Genre.ADVENTURE, Genre.DRAMA), author = "نیلوفر",
        language = ComicLanguage.JA, status = PublicationStatus.COMPLETED,
        popularity = 300, addedOrder = 1
    )
    private val c = sampleComic(
        id = "c", title = "بازی\u200Cها", originalTitle = "GAMES 2",
        type = ComicType.WEBTOON, genres = listOf(Genre.COMEDY), author = "پارسا",
        language = ComicLanguage.FA, status = PublicationStatus.HIATUS,
        popularity = 200, addedOrder = 2
    )
    private val all = listOf(a, b, c)

    private fun ids(filters: SearchFilters) = filterAndSort(all, filters).map { it.id }

    @Test
    fun emptyQueryReturnsEverythingSortedByPopularity() {
        assertEquals(listOf("b", "c", "a"), ids(SearchFilters()))
    }

    @Test
    fun newestSortUsesAddedOrder() {
        assertEquals(listOf("a", "c", "b"), ids(SearchFilters(sort = SortOrder.NEWEST)))
    }

    @Test
    fun latinTitleSearchIgnoresCase() {
        assertEquals(listOf("a"), ids(SearchFilters(query = "starfall")))
    }

    @Test
    fun arabicKafMatchesPersianKaf() {
        assertEquals(listOf("a"), ids(SearchFilters(query = "\u0643تاب")))
    }

    @Test
    fun arabicYehMatchesPersianYeh() {
        assertEquals(listOf("b"), ids(SearchFilters(query = "ماجرا\u064A")))
    }

    @Test
    fun zwnjIsIgnored() {
        assertEquals(listOf("c"), ids(SearchFilters(query = "بازیها")))
    }

    @Test
    fun persianDigitsMatchLatinDigits() {
        assertEquals(listOf("c"), ids(SearchFilters(query = "\u06F2")))
    }

    @Test
    fun authorAndGenreAndTypeLabelsAreSearchable() {
        assertEquals(listOf("a"), ids(SearchFilters(query = "آرین")))
        assertEquals(listOf("c"), ids(SearchFilters(query = "کمدی")))
        assertEquals(listOf("b"), ids(SearchFilters(query = "مانگا")))
    }

    @Test
    fun eachFilterNarrowsResults() {
        assertEquals(listOf("b"), ids(SearchFilters(type = ComicType.MANGA)))
        assertEquals(listOf("b"), ids(SearchFilters(genre = Genre.DRAMA)))
        assertEquals(listOf("c"), ids(SearchFilters(status = PublicationStatus.HIATUS)))
        assertEquals(listOf("a"), ids(SearchFilters(language = ComicLanguage.EN)))
    }

    @Test
    fun filtersAreCombinedWithAnd() {
        assertTrue(ids(SearchFilters(type = ComicType.MANGA, language = ComicLanguage.EN)).isEmpty())
        assertEquals(
            listOf("b"),
            ids(SearchFilters(type = ComicType.MANGA, language = ComicLanguage.JA, query = "دریا"))
        )
    }

    @Test
    fun unknownQueryGivesEmptyList() {
        assertTrue(ids(SearchFilters(query = "چیزی که وجود ندارد")).isEmpty())
    }

    @Test
    fun hasActiveFiltersIgnoresSortOnly() {
        assertFalse(SearchFilters().hasActiveFilters)
        assertFalse(SearchFilters(sort = SortOrder.NEWEST).hasActiveFilters)
        assertTrue(SearchFilters(query = "x").hasActiveFilters)
        assertTrue(SearchFilters(genre = Genre.ACTION).hasActiveFilters)
        assertEquals(2, SearchFilters(genre = Genre.ACTION, type = ComicType.COMIC).activeFilterCount)
    }

    @Test
    fun normalizeHandlesMixedInput() {
        assertEquals("ایران 12 abc", normalizeForSearch("  \u0627\u064A\u0631\u0627\u0646 \u06F1\u0662 ABC "))
    }
}
