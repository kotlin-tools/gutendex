package com.github.kupolak.gutendex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class QueryBuilderTest {
    @Test
    fun `buildUrl should return base URL when no parameters`() {
        val queryBuilder = QueryBuilder()
        val url = queryBuilder.buildUrl("https://example.com")
        assertEquals("https://example.com", url)
    }

    @Test
    fun `buildUrl should append single parameter`() {
        val queryBuilder =
            QueryBuilder()
                .search("alice")
        val url = queryBuilder.buildUrl("https://example.com")
        assertEquals("https://example.com?search=alice", url)
    }

    @Test
    fun `buildUrl should append multiple parameters`() {
        val queryBuilder =
            QueryBuilder()
                .search("alice")
                .authorYearStart(1800)
                .sort(SortType.POPULAR)
        val url = queryBuilder.buildUrl("https://example.com")

        assertTrue(url.startsWith("https://example.com?"))
        assertTrue(url.contains("search=alice"))
        assertTrue(url.contains("author_year_start=1800"))
        assertTrue(url.contains("sort=popular"))
    }

    @Test
    fun `authorYearStart should set correct parameter`() {
        val queryBuilder = QueryBuilder().authorYearStart(1850)
        val url = queryBuilder.buildUrl("https://example.com")
        assertTrue(url.contains("author_year_start=1850"))
    }

    @Test
    fun `authorYearEnd should set correct parameter`() {
        val queryBuilder = QueryBuilder().authorYearEnd(1900)
        val url = queryBuilder.buildUrl("https://example.com")
        assertTrue(url.contains("author_year_end=1900"))
    }

    @Test
    fun `copyright should handle boolean values correctly`() {
        val trueBuilder = QueryBuilder().copyright(true)
        val falseBuilder = QueryBuilder().copyright(false)
        val nullBuilder = QueryBuilder().copyright(null)

        assertTrue(trueBuilder.buildUrl("https://example.com").contains("copyright=true"))
        assertTrue(falseBuilder.buildUrl("https://example.com").contains("copyright=false"))
        assertTrue(nullBuilder.buildUrl("https://example.com").contains("copyright=null"))
    }

    @Test
    fun `copyrightMultiple should handle list of boolean values`() {
        val queryBuilder = QueryBuilder().copyrightMultiple(listOf(true, false, null))
        val url = queryBuilder.buildUrl("https://example.com")
        assertTrue(url.contains("copyright=true%2Cfalse%2Cnull"))
    }

    @Test
    fun `ids should handle list of integers`() {
        val queryBuilder = QueryBuilder().ids(listOf(1, 2, 3))
        val url = queryBuilder.buildUrl("https://example.com")
        assertTrue(url.contains("ids=1%2C2%2C3"))
    }

    @Test
    fun `languages should handle list of language codes`() {
        val queryBuilder = QueryBuilder().languages(listOf("en", "fr", "de"))
        val url = queryBuilder.buildUrl("https://example.com")
        assertTrue(url.contains("languages=en%2Cfr%2Cde"))
    }

    @Test
    fun `mimeType should set correct parameter`() {
        val queryBuilder = QueryBuilder().mimeType("text/html")
        val url = queryBuilder.buildUrl("https://example.com")
        assertTrue(url.contains("mime_type=text%2Fhtml"))
    }

    @Test
    fun `search should handle spaces and special characters`() {
        val queryBuilder = QueryBuilder().search("alice in wonderland")
        val url = queryBuilder.buildUrl("https://example.com")
        assertTrue(url.contains("search=alice+in+wonderland"))
    }

    @Test
    fun `sort should handle all enum values`() {
        val ascendingBuilder = QueryBuilder().sort(SortType.ASCENDING)
        val descendingBuilder = QueryBuilder().sort(SortType.DESCENDING)
        val popularBuilder = QueryBuilder().sort(SortType.POPULAR)

        assertTrue(ascendingBuilder.buildUrl("https://example.com").contains("sort=ascending"))
        assertTrue(descendingBuilder.buildUrl("https://example.com").contains("sort=descending"))
        assertTrue(popularBuilder.buildUrl("https://example.com").contains("sort=popular"))
    }

    @Test
    fun `topic should set correct parameter`() {
        val queryBuilder = QueryBuilder().topic("children")
        val url = queryBuilder.buildUrl("https://example.com")
        assertTrue(url.contains("topic=children"))
    }

    @Test
    fun `reset should clear all parameters`() {
        val queryBuilder =
            QueryBuilder()
                .search("test")
                .authorYearStart(1800)
                .reset()
        val url = queryBuilder.buildUrl("https://example.com")
        assertEquals("https://example.com", url)
    }

    @Test
    fun `chaining should work correctly`() {
        val queryBuilder =
            QueryBuilder()
                .search("dickens")
                .authorYearStart(1800)
                .authorYearEnd(1900)
                .languages(listOf("en"))
                .copyright(false)
                .sort(SortType.POPULAR)

        val url = queryBuilder.buildUrl("https://example.com")

        assertTrue(url.startsWith("https://example.com?"))
        assertTrue(url.contains("search=dickens"))
        assertTrue(url.contains("author_year_start=1800"))
        assertTrue(url.contains("author_year_end=1900"))
        assertTrue(url.contains("languages=en"))
        assertTrue(url.contains("copyright=false"))
        assertTrue(url.contains("sort=popular"))
    }
}
