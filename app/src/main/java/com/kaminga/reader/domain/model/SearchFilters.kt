package com.kaminga.reader.domain.model

enum class SortOrder(val label: String) {
    POPULAR("محبوب‌ترین"),
    NEWEST("جدیدترین")
}

data class SearchFilters(
    val query: String = "",
    val type: ComicType? = null,
    val genre: Genre? = null,
    val status: PublicationStatus? = null,
    val language: ComicLanguage? = null,
    val sort: SortOrder = SortOrder.POPULAR
) {
    /** تعداد فیلترهای انتخاب‌شده (بدون متن جست‌وجو و مرتب‌سازی). */
    val activeFilterCount: Int
        get() = listOfNotNull(type, genre, status, language).size

    /** آیا چیزی برای «پاک کردن» وجود دارد؟ مرتب‌سازی فیلتر حساب نمی‌شود. */
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || activeFilterCount > 0
}
