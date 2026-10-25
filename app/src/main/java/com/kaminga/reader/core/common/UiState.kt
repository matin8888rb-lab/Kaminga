package com.kaminga.reader.core.common

/** حالت‌های یک صفحه که داده را از مخزن می‌گیرد. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val retryable: Boolean = true) : UiState<Nothing>
}

object UiMessages {
    const val LOAD_FAILED = "بارگذاری اطلاعات انجام نشد. اتصال اینترنت را بررسی کن و دوباره تلاش کن."
    const val NOT_FOUND = "این اثر پیدا نشد. ممکن است حذف یا منتقل شده باشد."
    const val CHAPTER_NOT_FOUND = "این فصل پیدا نشد یا صفحه‌ای ندارد."
    const val IMPORT_DONE = "فصل شخصی اضافه شد."
    const val IMPORT_FAILED = "افزودن فصل انجام نشد. عکس معتبری انتخاب نشده یا فضای گوشی کافی نیست."
}
