package com.kaminga.reader.domain.repository

import com.kaminga.reader.domain.model.Comic

class ComicNotFoundException(val comicId: String) :
    NoSuchElementException("Comic not found: $comicId")

interface ComicRepository {
    suspend fun getComics(): Result<List<Comic>>
    suspend fun getComic(id: String): Result<Comic>
}
