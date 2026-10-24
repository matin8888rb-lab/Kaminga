package com.kaminga.reader.domain.model

data class Chapter(
    val id: String,
    val number: Int,
    val title: String
)

/**
 * یک اثر. `addedOrder` هرچه بزرگ‌تر باشد اثر جدیدتر است.
 * `isDemo` فقط برای داده‌ی نمایشی true می‌شود و در رابط کاربری برچسب می‌خورد.
 */
data class Comic(
    val id: String,
    val title: String,
    val originalTitle: String,
    val summary: String,
    val type: ComicType,
    val genres: List<Genre>,
    val author: String,
    val artist: String,
    val language: ComicLanguage,
    val status: PublicationStatus,
    val chapters: List<Chapter> = emptyList(),
    val popularity: Int = 0,
    val addedOrder: Int = 0,
    val accentColor: Long = 0xFF3A3F5C,
    val isFeatured: Boolean = false,
    val isDemo: Boolean = false
)
