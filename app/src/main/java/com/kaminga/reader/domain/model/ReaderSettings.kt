package com.kaminga.reader.domain.model

enum class ReaderMode(val label: String) {
    VERTICAL("عمودی"),
    PAGED("صفحه‌به‌صفحه")
}

enum class ReadingDirection(val label: String) {
    RTL("راست‌به‌چپ"),
    LTR("چپ‌به‌راست")
}

/** وب‌تون و مانهوا پیش‌فرض عمودی‌اند؛ کمیک و مانگا صفحه‌به‌صفحه. */
fun ComicType.defaultReaderMode(): ReaderMode = when (this) {
    ComicType.WEBTOON, ComicType.MANHWA -> ReaderMode.VERTICAL
    ComicType.COMIC, ComicType.MANGA -> ReaderMode.PAGED
}

/** فقط مانگا پیش‌فرض راست‌به‌چپ است. کاربر در مطالعه‌گر می‌تواند عوضش کند. */
fun ComicType.defaultReadingDirection(): ReadingDirection =
    if (this == ComicType.MANGA) ReadingDirection.RTL else ReadingDirection.LTR
