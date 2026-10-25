package com.kaminga.reader.domain.model

/**
 * یک فصل. فصل نمایشی صفحه‌ی ساخته‌شده داخل برنامه دارد (pagePaths خالی)،
 * فصل شخصی صفحه‌هایش فایل‌های کپی‌شده‌ی کاربر در حافظه‌ی برنامه است.
 */
data class Chapter(
    val id: String,
    val number: Int,
    val title: String,
    val pageCount: Int = 0,
    val pagePaths: List<String> = emptyList(),
    val isPersonal: Boolean = false
) {
    val isReadable: Boolean
        get() = pageCount > 0
}

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
