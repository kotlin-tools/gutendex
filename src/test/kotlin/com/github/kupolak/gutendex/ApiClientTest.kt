package com.github.kupolak.gutendex

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApiClientTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiClient: ApiClient

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        apiClient =
            ApiClient(
                mockWebServer.url("/").toString().trimEnd('/'),
            )
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
        apiClient.close()
    }

    @Test
    fun `getBooks should return successful response`() =
        runTest {
            val mockResponse =
                """
                {
                    "count": 1,
                    "next": null,
                    "previous": null,
                    "results": [
                        {
                            "id": 1,
                            "title": "Test Book",
                            "subjects": ["Fiction"],
                            "authors": [
                                {
                                    "name": "Test Author",
                                    "birth_year": 1850,
                                    "death_year": 1900
                                }
                            ],
                            "summaries": ["A test book"],
                            "translators": [],
                            "bookshelves": ["Fiction"],
                            "languages": ["en"],
                            "copyright": false,
                            "media_type": "Text",
                            "formats": {
                                "text/html": "http://example.com/1.html"
                            },
                            "download_count": 100
                        }
                    ]
                }
                """.trimIndent()

            mockWebServer.enqueue(
                MockResponse().setBody(mockResponse),
            )

            val result = apiClient.getBooks()

            assertTrue(result.isSuccess)
            val response = result.getOrNull()!!
            assertEquals(1, response.count)
            assertEquals(1, response.results.size)
            assertEquals("Test Book", response.results[0].title)
            assertEquals("Test Author", response.results[0].authors[0].name)
        }

    @Test
    fun `getBooks should handle query parameters`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody("""{"count": 0, "results": []}"""))

            val query = QueryBuilder()
                .search("alice")
                .authorYearStart(1800)
            val result = apiClient.getBooks(query)

            assertTrue(result.isSuccess)

            val request = mockWebServer.takeRequest()
            assertTrue(request.path?.contains("search=alice") == true)
            assertTrue(request.path?.contains("author_year_start=1800") == true)
        }

    @Test
    fun `getBook should return single book`() =
        runTest {
            val mockResponse =
                """
                {
                    "id": 1342,
                    "title": "Pride and Prejudice",
                    "subjects": ["England -- Fiction"],
                    "authors": [
                        {
                            "name": "Austen, Jane",
                            "birth_year": 1775,
                            "death_year": 1817
                        }
                    ],
                    "summaries": ["A classic romance novel"],
                    "translators": [],
                    "bookshelves": ["Best Books Ever Listings"],
                    "languages": ["en"],
                    "copyright": false,
                    "media_type": "Text",
                    "formats": {
                        "text/html": "http://example.com/1342.html",
                        "application/epub+zip": "http://example.com/1342.epub"
                    },
                    "download_count": 50000
                }
                """.trimIndent()

            mockWebServer.enqueue(MockResponse().setBody(mockResponse))

            val result = apiClient.getBook(1342)

            assertTrue(result.isSuccess)
            val book = result.getOrNull()!!
            assertEquals(1342, book.id)
            assertEquals("Pride and Prejudice", book.title)
            assertEquals("Austen, Jane", book.authors[0].name)
            assertEquals(1775, book.authors[0].birthYear)
            assertEquals(1817, book.authors[0].deathYear)
            assertEquals(50000, book.downloadCount)
            assertTrue(book.formats.containsKey("text/html"))
            assertTrue(book.formats.containsKey("application/epub+zip"))
        }

    @Test
    fun `getBook should handle error response`() =
        runTest {
            val errorResponse =
                """
                {
                    "detail": "No Book matches the given query."
                }
                """.trimIndent()

            mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody(errorResponse))

            val result = apiClient.getBook(999999)

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is GutendexException)
            assertEquals("No Book matches the given query.", exception?.message)
            assertEquals(404, (exception as GutendexException).httpCode)
        }

    @Test
    fun `getBooksFromUrl should work with full URL`() =
        runTest {
            val mockResponse =
                """
                {
                    "count": 2,
                    "next": "http://example.com/books/?page=3",
                    "previous": "http://example.com/books/?page=1",
                    "results": []
                }
                """.trimIndent()

            mockWebServer.enqueue(MockResponse().setBody(mockResponse))

            val result = apiClient.getBooksFromUrl("${mockWebServer.url("/books/?page=2")}")

            assertTrue(result.isSuccess)
            val response = result.getOrNull()!!
            assertEquals(2, response.count)
            assertEquals("http://example.com/books/?page=3", response.next)
            assertEquals("http://example.com/books/?page=1", response.previous)
        }

    @Test
    fun `should handle network error`() =
        runTest {
            mockWebServer.shutdown() // Force network error

            val result = apiClient.getBooks()

            assertTrue(result.isFailure)
            assertNotNull(result.exceptionOrNull())
        }

    @Test
    fun `should handle malformed JSON response`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody("invalid json"))

            val result = apiClient.getBooks()

            assertTrue(result.isFailure)
            assertNotNull(result.exceptionOrNull())
        }

    @Test
    fun `should handle empty response body`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setBody(""))

            val result = apiClient.getBooks()

            assertTrue(result.isFailure)
            assertNotNull(result.exceptionOrNull())
        }

    @Test
    fun `should handle server error with unknown format`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

            val result = apiClient.getBooks()

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is GutendexException)
            assertTrue(exception?.message?.startsWith("HTTP 500:") == true)
            assertEquals(500, (exception as GutendexException).httpCode)
        }
}
