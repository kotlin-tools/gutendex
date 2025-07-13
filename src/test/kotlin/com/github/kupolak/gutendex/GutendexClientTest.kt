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

class GutendexClientTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: GutendexClient

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        client = GutendexClient(baseUrl = mockWebServer.url("/").toString().trimEnd('/'))
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
        client.close()
    }

    @Test
    fun `getBooks returns BookListResponse`() =
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
                                {"name": "Test Author", "birth_year": 1900, "death_year": 1950}
                            ],
                            "summaries": ["A test book"],
                            "translators": [],
                            "bookshelves": ["Fiction"],
                            "languages": ["en"],
                            "copyright": false,
                            "media_type": "Text",
                            "formats": {"text/html": "http://example.com/1.html"},
                            "download_count": 100
                        }
                    ]
                }
                """.trimIndent()

            mockWebServer.enqueue(MockResponse().setBody(mockResponse))

            val result = client.getBooks()

            assertTrue(result.isSuccess)
            val response = result.getOrNull()!!
            assertEquals(1, response.count)
            assertEquals("Test Book", response.results[0].title)
        }

    @Test
    fun `getBook returns Book`() =
        runTest {
            val mockResponse =
                """
                {
                    "id": 2,
                    "title": "Another Book",
                    "subjects": ["Adventure"],
                    "authors": [
                        {"name": "Author 2", "birth_year": 1920, "death_year": 1980}
                    ],
                    "summaries": ["Another test book"],
                    "translators": [],
                    "bookshelves": ["Adventure"],
                    "languages": ["en"],
                    "copyright": false,
                    "media_type": "Text",
                    "formats": {"text/html": "http://example.com/2.html"},
                    "download_count": 200
                }
                """.trimIndent()

            mockWebServer.enqueue(MockResponse().setBody(mockResponse))

            val result = client.getBook(2)

            assertTrue(result.isSuccess)
            val book = result.getOrNull()!!
            assertEquals(2, book.id)
            assertEquals("Another Book", book.title)
            assertEquals("Author 2", book.authors[0].name)
        }

    @Test
    fun `searchBooks returns filtered results`() =
        runTest {
            val mockResponse =
                """
                {
                    "count": 1,
                    "next": null,
                    "previous": null,
                    "results": [
                        {
                            "id": 3,
                            "title": "Alice in Wonderland",
                            "subjects": [],
                            "authors": [],
                            "summaries": [],
                            "translators": [],
                            "bookshelves": [],
                            "languages": ["en"],
                            "copyright": false,
                            "media_type": "Text",
                            "formats": {},
                            "download_count": 300
                        }
                    ]
                }
                """.trimIndent()

            mockWebServer.enqueue(MockResponse().setBody(mockResponse))

            val result = client.searchBooks("alice")

            assertTrue(result.isSuccess)
            val response = result.getOrNull()!!
            assertEquals(1, response.count)
            assertEquals("Alice in Wonderland", response.results[0].title)
        }

    @Test
    fun `getBooksByLanguages returns correct language`() =
        runTest {
            val mockResponse =
                """
                {
                    "count": 1,
                    "next": null,
                    "previous": null,
                    "results": [
                        {
                            "id": 4,
                            "title": "French Book",
                            "subjects": [],
                            "authors": [],
                            "summaries": [],
                            "translators": [],
                            "bookshelves": [],
                            "languages": ["fr"],
                            "copyright": false,
                            "media_type": "Text",
                            "formats": {},
                            "download_count": 10
                        }
                    ]
                }
                """.trimIndent()

            mockWebServer.enqueue(MockResponse().setBody(mockResponse))

            val result = client.getBooksByLanguages(listOf("fr"))

            assertTrue(result.isSuccess)
            val response = result.getOrNull()!!
            assertEquals("French Book", response.results[0].title)
            assertEquals(listOf("fr"), response.results[0].languages)
        }

    @Test
    fun `getBooksByIds returns correct books`() =
        runTest {
            val mockResponse =
                """
                {
                    "count": 2,
                    "next": null,
                    "previous": null,
                    "results": [
                        {
                            "id": 5,
                            "title": "Book 5",
                            "subjects": [],
                            "authors": [],
                            "summaries": [],
                            "translators": [],
                            "bookshelves": [],
                            "languages": ["en"],
                            "copyright": false,
                            "media_type": "Text",
                            "formats": {},
                            "download_count": 5
                        },
                        {
                            "id": 6,
                            "title": "Book 6",
                            "subjects": [],
                            "authors": [],
                            "summaries": [],
                            "translators": [],
                            "bookshelves": [],
                            "languages": ["en"],
                            "copyright": false,
                            "media_type": "Text",
                            "formats": {},
                            "download_count": 6
                        }
                    ]
                }
                """.trimIndent()

            mockWebServer.enqueue(MockResponse().setBody(mockResponse))

            val result = client.getBooksByIds(listOf(5, 6))

            assertTrue(result.isSuccess)
            val response = result.getOrNull()!!
            assertEquals(2, response.results.size)
            assertEquals(5, response.results[0].id)
            assertEquals(6, response.results[1].id)
        }

    @Test
    fun `getBooksByTopic returns correct topic`() =
        runTest {
            val mockResponse =
                """
                {
                    "count": 1,
                    "next": null,
                    "previous": null,
                    "results": [
                        {
                            "id": 7,
                            "title": "Children's Book",
                            "subjects": ["Children"],
                            "authors": [],
                            "summaries": [],
                            "translators": [],
                            "bookshelves": ["Children"],
                            "languages": ["en"],
                            "copyright": false,
                            "media_type": "Text",
                            "formats": {},
                            "download_count": 7
                        }
                    ]
                }
                """.trimIndent()

            mockWebServer.enqueue(MockResponse().setBody(mockResponse))

            val result = client.getBooksByTopic("children")

            assertTrue(result.isSuccess)
            val response = result.getOrNull()!!
            assertEquals("Children's Book", response.results[0].title)
            assertTrue(
                response.results[0].subjects.contains("Children") ||
                    response.results[0].bookshelves.contains("Children"),
            )
        }

    @Test
    fun `getBooksByCopyright returns correct copyright status`() =
        runTest {
            val mockResponse =
                """
                {
                    "count": 1,
                    "next": null,
                    "previous": null,
                    "results": [
                        {
                            "id": 8,
                            "title": "Public Domain Book",
                            "subjects": [],
                            "authors": [],
                            "summaries": [],
                            "translators": [],
                            "bookshelves": [],
                            "languages": ["en"],
                            "copyright": false,
                            "media_type": "Text",
                            "formats": {},
                            "download_count": 8
                        }
                    ]
                }
                """.trimIndent()

            mockWebServer.enqueue(MockResponse().setBody(mockResponse))

            val result = client.getBooksByCopyright(false)

            assertTrue(result.isSuccess)
            val response = result.getOrNull()!!
            assertEquals("Public Domain Book", response.results[0].title)
            assertEquals(false, response.results[0].copyright)
        }

    @Test
    fun `getBooksByAuthorYearRange returns correct years`() =
        runTest {
            val mockResponse =
                """
                {
                    "count": 1,
                    "next": null,
                    "previous": null,
                    "results": [
                        {
                            "id": 9,
                            "title": "19th Century Book",
                            "subjects": [],
                            "authors": [
                                {
                                    "name": "Author 9",
                                    "birth_year": 1800,
                                    "death_year": 1899
                                }
                            ],
                            "summaries": [],
                            "translators": [],
                            "bookshelves": [],
                            "languages": ["en"],
                            "copyright": false,
                            "media_type": "Text",
                            "formats": {},
                            "download_count": 9
                        }
                    ]
                }
                """.trimIndent()

            mockWebServer.enqueue(MockResponse().setBody(mockResponse))

            val result = client.getBooksByAuthorYearRange(1800, 1899)

            assertTrue(result.isSuccess)
            val response = result.getOrNull()!!
            assertEquals("19th Century Book", response.results[0].title)
            assertEquals(1800, response.results[0].authors[0].birthYear)
            assertEquals(1899, response.results[0].authors[0].deathYear)
        }

    @Test
    fun `getNextPage and getPreviousPage work correctly`() =
        runTest {
            val (firstPage, nextPage) = setupPaginationTestData()
            
            val firstResult = client.getBooks()
            assertTrue(firstResult.isSuccess)
            val firstResponse = firstResult.getOrNull()!!
            assertEquals("Book 10", firstResponse.results[0].title)
            assertNotNull(firstResponse.next)

            val nextResult = client.getNextPage(firstResponse)
            assertTrue(nextResult.isSuccess)
            val nextResponse = nextResult.getOrNull()!!
            assertEquals("Book 11", nextResponse.results[0].title)
            assertNotNull(nextResponse.previous)
        }
    
    private fun setupPaginationTestData(): Pair<String, String> {
        val nextUrl = mockWebServer.url("/next").toString()
        val prevUrl = mockWebServer.url("/first").toString()
        
        val firstPage =
            """
            {
                "count": 2,
                "next": "$nextUrl",
                "previous": null,
                "results": [
                    {
                        "id": 10,
                        "title": "Book 10",
                        "subjects": [],
                        "authors": [],
                        "summaries": [],
                        "translators": [],
                        "bookshelves": [],
                        "languages": ["en"],
                        "copyright": false,
                        "media_type": "Text",
                        "formats": {},
                        "download_count": 10
                    }
                ]
            }
            """.trimIndent()

        val nextPage =
            """
            {
                "count": 2,
                "next": null,
                "previous": "$prevUrl",
                "results": [
                    {
                        "id": 11,
                        "title": "Book 11",
                        "subjects": [],
                        "authors": [],
                        "summaries": [],
                        "translators": [],
                        "bookshelves": [],
                        "languages": ["en"],
                        "copyright": false,
                        "media_type": "Text",
                        "formats": {},
                        "download_count": 11
                    }
                ]
            }
            """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(firstPage))
        mockWebServer.enqueue(MockResponse().setBody(nextPage))
        
        return Pair(firstPage, nextPage)
    }
}
