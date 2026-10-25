package com.kaminga.reader.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kaminga.reader.domain.model.ImportedChapter
import com.kaminga.reader.domain.model.ReadingProgress
import com.kaminga.reader.domain.repository.UserLibraryRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.libraryDataStore: DataStore<Preferences> by preferencesDataStore(name = "kaminga_library")

private val FAVORITES_KEY = stringSetPreferencesKey("favorites")
private val PROGRESS_KEY = stringPreferencesKey("progress")
private val IMPORTED_KEY = stringPreferencesKey("imported_chapters")

class DataStoreUserLibraryRepository(context: Context) : UserLibraryRepository {

    private val store: DataStore<Preferences> = context.applicationContext.libraryDataStore

    private val data: Flow<Preferences> = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    override val favoriteIds: Flow<Set<String>> =
        data.map { prefs -> prefs[FAVORITES_KEY] ?: emptySet() }.distinctUntilChanged()

    override val progress: Flow<Map<String, ReadingProgress>> =
        data.map { prefs -> LibraryCodecs.decodeProgress(prefs[PROGRESS_KEY]) }.distinctUntilChanged()

    override val importedChapters: Flow<List<ImportedChapter>> =
        data.map { prefs -> LibraryCodecs.decodeImported(prefs[IMPORTED_KEY]) }.distinctUntilChanged()

    override suspend fun setFavorite(comicId: String, favorite: Boolean) {
        store.edit { prefs ->
            val current = prefs[FAVORITES_KEY] ?: emptySet()
            prefs[FAVORITES_KEY] = if (favorite) current + comicId else current - comicId
        }
    }

    override suspend fun saveProgress(progress: ReadingProgress) {
        store.edit { prefs ->
            val map = LibraryCodecs.decodeProgress(prefs[PROGRESS_KEY]).toMutableMap()
            map[progress.comicId] = progress
            prefs[PROGRESS_KEY] = LibraryCodecs.encodeProgress(map)
        }
    }

    override suspend fun addImportedChapter(chapter: ImportedChapter) {
        store.edit { prefs ->
            val list = LibraryCodecs.decodeImported(prefs[IMPORTED_KEY]) + chapter
            prefs[IMPORTED_KEY] = LibraryCodecs.encodeImported(list)
        }
    }

    override suspend fun removeImportedChapter(chapterId: String) {
        store.edit { prefs ->
            val list = LibraryCodecs.decodeImported(prefs[IMPORTED_KEY]).filterNot { it.id == chapterId }
            prefs[IMPORTED_KEY] = LibraryCodecs.encodeImported(list)
        }
    }
}
