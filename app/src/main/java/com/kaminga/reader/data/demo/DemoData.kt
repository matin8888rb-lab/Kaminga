package com.kaminga.reader.data.demo

import com.kaminga.reader.domain.model.Chapter
import com.kaminga.reader.domain.model.Comic
import com.kaminga.reader.domain.model.ComicLanguage
import com.kaminga.reader.domain.model.ComicType
import com.kaminga.reader.domain.model.Genre
import com.kaminga.reader.domain.model.PublicationStatus

/**
 * آثار کاملاً ساختگی برای آزمایش رابط کاربری. نام‌ها و داستان‌ها خیالی هستند
 * و هیچ‌کدام تصویر یا محتوای کپی‌رایتی ندارند.
 */
internal object DemoData {

    private fun chapters(comicId: String, count: Int): List<Chapter> =
        (1..count).map { number -> Chapter("$comicId-ch$number", number, "قسمت نمونه‌ی $number") }

    val comics: List<Comic> = listOf(
        Comic(
            id = "demo-starfall",
            title = "سقوط ستاره‌ها",
            originalTitle = "STARFALL",
            summary = "ماجرایی علمی‌تخیلی درباره‌ی ناخدایی که سیگنالی از یک جهان دور دریافت می‌کند. (اثر خیالی برای آزمایش رابط)",
            type = ComicType.COMIC,
            genres = listOf(Genre.SCI_FI, Genre.ADVENTURE),
            author = "آرین نمونه‌پور",
            artist = "کیان نمایشی",
            language = ComicLanguage.EN,
            status = PublicationStatus.ONGOING,
            chapters = chapters("demo-starfall", 6),
            popularity = 920,
            addedOrder = 8,
            accentColor = 0xFF6447B8,
            isFeatured = true,
            isDemo = true
        ),
        Comic(
            id = "demo-neon-district",
            title = "منطقه‌ی نئون",
            originalTitle = "NEON DISTRICT",
            summary = "شهری آینده‌نگر که هر انتخاب در آن مسیر داستان را عوض می‌کند. (اثر خیالی برای آزمایش رابط)",
            type = ComicType.MANHWA,
            genres = listOf(Genre.ACTION, Genre.SCI_FI),
            author = "نیلوفر آزمایشی",
            artist = "نیلوفر آزمایشی",
            language = ComicLanguage.KO,
            status = PublicationStatus.ONGOING,
            chapters = chapters("demo-neon-district", 12),
            popularity = 870,
            addedOrder = 7,
            accentColor = 0xFF176D7A,
            isFeatured = true,
            isDemo = true
        ),
        Comic(
            id = "demo-moon-archive",
            title = "بایگانی ماه",
            originalTitle = "MOON ARCHIVE",
            summary = "معمایی زیر نور ماه درباره‌ی کتابخانه‌ای که شب‌ها خودش را عوض می‌کند. (اثر خیالی برای آزمایش رابط)",
            type = ComicType.MANGA,
            genres = listOf(Genre.MYSTERY, Genre.DRAMA),
            author = "پارسا نمونه‌ساز",
            artist = "دریا نمایشی",
            language = ComicLanguage.JA,
            status = PublicationStatus.COMPLETED,
            chapters = chapters("demo-moon-archive", 24),
            popularity = 780,
            addedOrder = 3,
            accentColor = 0xFF9B4F65,
            isFeatured = true,
            isDemo = true
        ),
        Comic(
            id = "demo-paper-kingdom",
            title = "پادشاهی کاغذی",
            originalTitle = "PAPER KINGDOM",
            summary = "دختری در سرزمینی از کاغذ و جوهر راه می‌افتد تا نقشه‌ی گمشده‌ی پادشاهی را پیدا کند. (اثر خیالی برای آزمایش رابط)",
            type = ComicType.WEBTOON,
            genres = listOf(Genre.FANTASY, Genre.ADVENTURE),
            author = "آوا آزمایشی",
            artist = "سینا نمونه‌پور",
            language = ComicLanguage.FA,
            status = PublicationStatus.ONGOING,
            chapters = chapters("demo-paper-kingdom", 9),
            popularity = 640,
            addedOrder = 6,
            accentColor = 0xFF7A5A1E,
            isDemo = true
        ),
        Comic(
            id = "demo-last-lantern",
            title = "آخرین فانوس",
            originalTitle = "LAST LANTERN",
            summary = "نگهبان فانوس دریایی راز نوری را کشف می‌کند که کشتی‌ها را به جزیره‌ای ناشناخته می‌برد. (اثر خیالی برای آزمایش رابط)",
            type = ComicType.MANGA,
            genres = listOf(Genre.FANTASY, Genre.DRAMA),
            author = "سینا نمونه‌پور",
            artist = "سینا نمونه‌پور",
            language = ComicLanguage.JA,
            status = PublicationStatus.HIATUS,
            chapters = chapters("demo-last-lantern", 15),
            popularity = 560,
            addedOrder = 2,
            accentColor = 0xFF2F5D8A,
            isDemo = true
        ),
        Comic(
            id = "demo-clockwork-cafe",
            title = "کافه‌ی چرخ‌دنده",
            originalTitle = "CLOCKWORK CAFE",
            summary = "کافه‌ای که پیشخدمت‌هایش ربات‌اند و مشتری‌هایش هر شب یک ماجرای عجیب با خودشان می‌آورند. (اثر خیالی برای آزمایش رابط)",
            type = ComicType.WEBTOON,
            genres = listOf(Genre.COMEDY, Genre.DRAMA),
            author = "تینا نمونه‌ساز",
            artist = "دریا نمایشی",
            language = ComicLanguage.FA,
            status = PublicationStatus.ONGOING,
            chapters = chapters("demo-clockwork-cafe", 18),
            popularity = 710,
            addedOrder = 5,
            accentColor = 0xFF7A3B5E,
            isDemo = true
        ),
        Comic(
            id = "demo-iron-tide",
            title = "جزر و مد آهنین",
            originalTitle = "IRON TIDE",
            summary = "ماجرای کوتاه و فشرده‌ی یک بازرس در بندری که کشتی‌هایش بی‌خبر ناپدید می‌شوند. (اثر خیالی برای آزمایش رابط)",
            type = ComicType.COMIC,
            genres = listOf(Genre.ACTION, Genre.MYSTERY),
            author = "آرین نمونه‌پور",
            artist = "آوا آزمایشی",
            language = ComicLanguage.EN,
            status = PublicationStatus.COMPLETED,
            chapters = chapters("demo-iron-tide", 5),
            popularity = 430,
            addedOrder = 1,
            accentColor = 0xFF3C5A47,
            isDemo = true
        ),
        Comic(
            id = "demo-dawn-signal",
            title = "سیگنال سپیده‌دم",
            originalTitle = "DAWN SIGNAL",
            summary = "اثری که هنوز فصلی منتشر نکرده؛ برای آزمایش حالت «بدون فصل». (اثر خیالی برای آزمایش رابط)",
            type = ComicType.MANHWA,
            genres = listOf(Genre.SCI_FI, Genre.MYSTERY),
            author = "کیان نمایشی",
            artist = "نیلوفر آزمایشی",
            language = ComicLanguage.KO,
            status = PublicationStatus.UPCOMING,
            chapters = emptyList(),
            popularity = 150,
            addedOrder = 9,
            accentColor = 0xFF4A4A8F,
            isDemo = true
        )
    )
}
