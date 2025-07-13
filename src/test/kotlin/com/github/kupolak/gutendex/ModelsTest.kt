package com.github.kupolak.gutendex

import com.github.kupolak.gutendex.models.Book
import com.github.kupolak.gutendex.models.BookListResponse
import com.github.kupolak.gutendex.models.ErrorResponse
import com.github.kupolak.gutendex.models.Person
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ModelsTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    @Test
    fun `Person serializes and deserializes correctly`() {
        val person =
            Person(
                birthYear = 1900,
                deathYear = 1950,
                name = "John Doe",
            )
        val serialized = json.encodeToString(person)
        val deserialized = json.decodeFromString<Person>(serialized)
        assertEquals(person, deserialized)
    }

    @Test
    fun `Book serializes and deserializes correctly`() {
        val book =
            Book(
                id = 1,
                title = "Test Book",
                subjects = listOf("Fiction", "Adventure"),
                authors = listOf(Person(1900, 1950, "John Doe")),
                summaries = listOf("A test book"),
                translators = listOf(Person(1920, 1980, "Jane Smith")),
                bookshelves = listOf("Fiction"),
                languages = listOf("en", "fr"),
                copyright = false,
                mediaType = "Text",
                formats = mapOf("text/html" to "http://example.com/1.html"),
                downloadCount = 123,
            )
        val serialized = json.encodeToString(book)
        val deserialized = json.decodeFromString<Book>(serialized)
        assertEquals(book, deserialized)
    }

    @Test
    fun `BookListResponse serializes and deserializes correctly`() {
        val book =
            Book(
                id = 2,
                title = "Another Book",
                subjects = listOf("Drama"),
                authors = listOf(Person(1910, 1990, "Alice")),
                summaries = listOf("Another summary"),
                translators = emptyList(),
                bookshelves = listOf("Drama"),
                languages = listOf("en"),
                copyright = null,
                mediaType = "Text",
                formats = mapOf("text/html" to "http://example.com/2.html"),
                downloadCount = 42,
            )
        val response =
            BookListResponse(
                count = 1,
                next = "http://example.com/next",
                previous = null,
                results = listOf(book),
            )
        val serialized = json.encodeToString(response)
        val deserialized = json.decodeFromString<BookListResponse>(serialized)
        assertEquals(response, deserialized)
    }

    @Test
    fun `ErrorResponse serializes and deserializes correctly`() {
        val error = ErrorResponse(detail = "Something went wrong")
        val serialized = json.encodeToString(error)
        val deserialized = json.decodeFromString<ErrorResponse>(serialized)
        assertEquals(error, deserialized)
    }

    @Test
    fun `Book equals and hashCode work as expected`() {
        val book1 =
            Book(
                id = 1,
                title = "Book",
                subjects = listOf("Fiction"),
                authors = listOf(Person(1900, 1950, "John Doe")),
                summaries = listOf("Summary"),
                translators = emptyList(),
                bookshelves = listOf("Fiction"),
                languages = listOf("en"),
                copyright = false,
                mediaType = "Text",
                formats = mapOf("text/html" to "url"),
                downloadCount = 10,
            )
        val book2 = book1.copy()
        assertEquals(book1, book2)
        assertEquals(book1.hashCode(), book2.hashCode())
    }

    @Test
    fun `Person equals and hashCode work as expected`() {
        val p1 = Person(1900, 1950, "John Doe")
        val p2 = Person(1900, 1950, "John Doe")
        assertEquals(p1, p2)
        assertEquals(p1.hashCode(), p2.hashCode())
    }
}
