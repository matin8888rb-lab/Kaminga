package com.kaminga.reader.domain.model

enum class ComicType(val label: String) {
    COMIC("کمیک"),
    MANGA("مانگا"),
    MANHWA("مانهوا"),
    WEBTOON("وب‌تون")
}

enum class Genre(val label: String) {
    ACTION("اکشن"),
    SCI_FI("علمی‌تخیلی"),
    FANTASY("فانتزی"),
    MYSTERY("معمایی"),
    DRAMA("درام"),
    ADVENTURE("ماجراجویی"),
    COMEDY("کمدی")
}

enum class PublicationStatus(val label: String) {
    ONGOING("در حال انتشار"),
    COMPLETED("تکمیل‌شده"),
    HIATUS("متوقف‌شده"),
    UPCOMING("به‌زودی")
}

enum class ComicLanguage(val label: String) {
    FA("فارسی"),
    EN("انگلیسی"),
    JA("ژاپنی"),
    KO("کره‌ای")
}
