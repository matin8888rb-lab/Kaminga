package com.kaminga.reader.data

import com.kaminga.reader.data.local.LibraryCodecs
import com.kaminga.reader.domain.model.ImportedChapter
import com.kaminga.reader.domain.model.ReadingProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryCodecsTest {

    @Test
    fun progressSurvivesEncodeAndDecode() {
        val original = mapOf(
            "a" to ReadingProgress("a", "a-2", 2, 3, 10, 1000L),
            "b" to ReadingProgress("b", "uuid-with-dashes", 7, 0, 1, 2000L)
        )
        assertEquals(original, LibraryCodecs.decodeProgress(LibraryCodecs.encodeProgress(original)))
    }

    @Test
    fun emptyOrMissingTextDecodesToEmpty() {
        assertTrue(LibraryCodecs.decodeProgress(null).isEmpty())
        assertTrue(LibraryCodecs.decodeProgress("").isEmpty())
        assertTrue(LibraryCodecs.decodeImported(null).isEmpty())
        assertTrue(LibraryCodecs.decodeImported("").isEmpty())
        assertEquals("", LibraryCodecs.encodeProgress(emptyMap()))
        assertEquals("", LibraryCodecs.encodeImported(emptyList()))
    }

    @Test
    fun malformedProgressRecordsAreSkipped() {
        val good = ReadingProgress("a", "a-1", 1, 2, 5, 9L)
        val encoded = LibraryCodecs.encodeProgress(mapOf("a" to good))
        val broken = "garbage\u001Ea\u001Fa-1\u001Fnot-a-number\u001F2\u001F5\u001F9"
        val decoded = LibraryCodecs.decodeProgress(broken + "\u001E" + encoded)
        assertEquals(mapOf("a" to good), decoded)
    }

    @Test
    fun importedChaptersSurviveEncodeAndDecode() {
        val original = listOf(
            ImportedChapter("id1", "a", "فصل شخصی ۱", listOf("/data/x/page_0001.jpg", "/data/x/page_0002.png"), 11L),
            ImportedChapter("id2", "b", "دیگر", listOf("/data/y/page_0001.jpg"), 22L)
        )
        assertEquals(original, LibraryCodecs.decodeImported(LibraryCodecs.encodeImported(original)))
    }

    @Test
    fun separatorsInsideTextAreReplacedBySpaces() {
        val tricky = ImportedChapter("id", "a", "عنوان\u001Fعجیب\u001Eبا\u001Dجداکننده", listOf("p"), 1L)
        val decoded = LibraryCodecs.decodeImported(LibraryCodecs.encodeImported(listOf(tricky))).single()
        assertEquals("عنوان عجیب با جداکننده", decoded.title)
        assertEquals(listOf("p"), decoded.pagePaths)
    }

    @Test
    fun malformedImportedRecordsAreSkipped() {
        val good = ImportedChapter("id", "a", "t", listOf("p"), 5L)
        val encoded = LibraryCodecs.encodeImported(listOf(good))
        val decoded = LibraryCodecs.decodeImported("x\u001Fy\u001E" + encoded)
        assertEquals(listOf(good), decoded)
    }
}
