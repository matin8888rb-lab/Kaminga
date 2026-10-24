package com.kaminga.reader.domain.usecase

import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.model.SearchFilters
import com.kaminga.reader.domain.model.SortOrder

/**
 * متن را برای مقایسه‌ی جست‌وجو یکدست می‌کند:
 * ي و ى عربی -> ی، ك عربی -> ک، حذف نیم‌فاصله و کشیده و اعراب،
 * ارقام فارسی و عربی -> لاتین، حروف لاتین -> کوچک.
 */
fun normalizeForSearch(input: String): String {
    val out = StringBuilder(input.length)
    for (ch in input.trim()) {
        when (ch) {
            '\u064A', '\u0649' -> out.append('\u06CC')
            '\u0643' -> out.append('\u06A9')
            '\u200C', '\u200D', '\u0640' -> Unit
            in '\u064B'..'\u065F' -> Unit
            in '\u06F0'..'\u06F9' -> out.append('0' + (ch - '\u06F0'))
            in '\u0660'..'\u0669' -> out.append('0' + (ch - '\u0660'))
            else -> out.append(ch.lowercaseChar())
        }
    }
    return out.toString()
}

private fun Comic.searchableText(): String = normalizeForSearch(
    buildString {
        append(title).append(' ')
        append(originalTitle).append(' ')
        append(author).append(' ')
        append(artist).append(' ')
        append(type.label).append(' ')
        genres.forEach { append(it.label).append(' ') }
    }
)

/** فیلتر و مرتب‌سازی محلی؛ همه‌ی شرط‌ها با «و» ترکیب می‌شوند. */
fun filterAndSort(comics: List<Comic>, filters: SearchFilters): List<Comic> {
    val query = normalizeForSearch(filters.query)
    val matched = comics.filter { comic ->
        (filters.type == null || comic.type == filters.type) &&
            (filters.genre == null || filters.genre in comic.genres) &&
            (filters.status == null || comic.status == filters.status) &&
            (filters.language == null || comic.language == filters.language) &&
            (query.isEmpty() || comic.searchableText().contains(query))
    }
    return when (filters.sort) {
        SortOrder.POPULAR -> matched.sortedByDescending { it.popularity }
        SortOrder.NEWEST -> matched.sortedByDescending { it.addedOrder }
    }
}
