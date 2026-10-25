package com.kaminga.reader.data

import com.kaminga.reader.data.repository.DemoComicRepository
import com.kaminga.reader.domain.model.PublicationStatus
import com.kaminga.reader.domain.repository.ComicNotFoundException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoComicRepositoryTest {

    private val repository = DemoComicRepository(latencyMillis = 0)

    @Test
    fun comicsExistAndAreAllMarkedDemo() = runTest {
        val comics = repository.getComics().getOrThrow()
        assertTrue(comics.size >= 6)
        assertTrue(comics.all { it.isDemo })
    }

    @Test
    fun idsAreUnique() = runTest {
        val comics = repository.getComics().getOrThrow()
        assertEquals(comics.size, comics.map { it.id }.toSet().size)
    }

    @Test
    fun chapterNumbersStartAtOneAndAreSequential() = runTest {
        repository.getComics().getOrThrow().forEach { comic ->
            assertEquals(comic.chapters.indices.map { it + 1 }, comic.chapters.map { it.number })
        }
    }

    @Test
    fun everyDemoChapterIsReadable() = runTest {
        val comics = repository.getComics().getOrThrow()
        assertTrue(comics.all { comic -> comic.chapters.all { it.isReadable } })
    }

    @Test
    fun atLeastOneComicHasNoChaptersToExerciseEmptyState() = runTest {
        val comics = repository.getComics().getOrThrow()
        assertTrue(comics.any { it.chapters.isEmpty() && it.status == PublicationStatus.UPCOMING })
    }

    @Test
    fun existingComicIsFound() = runTest {
        val comic = repository.getComic("demo-starfall").getOrThrow()
        assertEquals("STARFALL", comic.originalTitle)
    }

    @Test
    fun missingComicFailsWithNotFound() = runTest {
        val error = repository.getComic("does-not-exist").exceptionOrNull()
        assertTrue(error is ComicNotFoundException)
    }
}
