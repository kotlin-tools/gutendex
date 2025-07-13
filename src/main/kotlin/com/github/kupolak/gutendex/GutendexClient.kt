@file:Suppress("ktlint:standard:no-trailing-spaces", "ktlint:standard:comment-spacing")

package com.github.kupolak.gutendex

import com.github.kupolak.gutendex.models.Book
import com.github.kupolak.gutendex.models.BookListResponse

/**
 * Main client for interacting with the Gutendex API
 * 
 * @param baseUrl The base URL for the API (default: https://gutendex.com)
 * @param enableLogging Whether to enable HTTP logging for debugging
 */
class GutendexClient(
    baseUrl: String = "https://gutendex.com",
    enableLogging: Boolean = false
) {
    private val apiClient = ApiClient(baseUrl, enableLogging)
    private val paginationHelper = PaginationHelper(apiClient)
    private val searchHelper = SearchHelper(this)
    
    /**
     * Get a list of books with optional filters
     * 
     * @param queryBuilder Optional query builder to filter results
     * @return Result containing BookListResponse or error
     */
    suspend fun getBooks(queryBuilder: QueryBuilder = QueryBuilder()): Result<BookListResponse> {
        return apiClient.getBooks(queryBuilder)
    }
    
    /**
     * Get a single book by its Project Gutenberg ID
     * 
     * @param id The Project Gutenberg ID of the book
     * @return Result containing Book or error
     */
    suspend fun getBook(id: Int): Result<Book> {
        return apiClient.getBook(id)
    }
    
    /**
     * Get books from a specific URL (useful for pagination)
     * 
     * @param url The full URL to fetch books from
     * @return Result containing BookListResponse or error
     */
    suspend fun getBooksFromUrl(url: String): Result<BookListResponse> {
        return apiClient.getBooksFromUrl(url)
    }
    
    /**
     * Get the next page of books from a BookListResponse
     * 
     * @param response The current BookListResponse
     * @return Result containing next page BookListResponse or error if no next page
     */
    suspend fun getNextPage(response: BookListResponse): Result<BookListResponse> {
        return paginationHelper.getNextPage(response)
    }
    
    /**
     * Get the previous page of books from a BookListResponse
     * 
     * @param response The current BookListResponse
     * @return Result containing previous page BookListResponse or error if no previous page
     */
    suspend fun getPreviousPage(response: BookListResponse): Result<BookListResponse> {
        return paginationHelper.getPreviousPage(response)
    }
    
    /**
     * Search for books by title and author
     * 
     * @param query The search query
     * @return Result containing BookListResponse or error
     */
    suspend fun searchBooks(query: String): Result<BookListResponse> {
        return searchHelper.searchBooks(query)
    }
    
    /**
     * Get books in specific languages
     * 
     * @param languages List of two-character language codes (e.g., ["en", "fr"])
     * @return Result containing BookListResponse or error
     */
    suspend fun getBooksByLanguages(languages: List<String>): Result<BookListResponse> {
        return searchHelper.getBooksByLanguages(languages)
    }
    
    /**
     * Get books by specific Project Gutenberg IDs
     * 
     * @param ids List of Project Gutenberg IDs
     * @return Result containing BookListResponse or error
     */
    suspend fun getBooksByIds(ids: List<Int>): Result<BookListResponse> {
        return searchHelper.getBooksByIds(ids)
    }
    
    /**
     * Get books by topic (searches in bookshelves and subjects)
     * 
     * @param topic The topic to search for
     * @return Result containing BookListResponse or error
     */
    suspend fun getBooksByTopic(topic: String): Result<BookListResponse> {
        return searchHelper.getBooksByTopic(topic)
    }
    
    /**
     * Get books by copyright status
     * 
     * @param copyrightStatus true for copyrighted books, false for public domain, null for unknown
     * @return Result containing BookListResponse or error
     */
    suspend fun getBooksByCopyright(copyrightStatus: Boolean?): Result<BookListResponse> {
        return searchHelper.getBooksByCopyright(copyrightStatus)
    }
    
    /**
     * Get books by author year range
     * 
     * @param startYear The start year (inclusive)
     * @param endYear The end year (inclusive)
     * @return Result containing BookListResponse or error
     */
    @Suppress("ktlint:standard:argument-list-wrapping")
    suspend fun getBooksByAuthorYearRange(
        startYear: Int? = null,
        endYear: Int? = null
    ): Result<BookListResponse> {
        return searchHelper.getBooksByAuthorYearRange(startYear, endYear)
    }
    
    /**
     * Create a new QueryBuilder for advanced queries
     * 
     * @return A new QueryBuilder instance
     */
    fun queryBuilder(): QueryBuilder = QueryBuilder()
    
    /**
     * Close the client and clean up resources
     */
    fun close() {
        apiClient.close()
    }
}

/**
 * Helper class for handling search and filter operations.
 */
internal class SearchHelper(private val client: GutendexClient) {
    suspend fun searchBooks(query: String): Result<BookListResponse> {
        return client.getBooks(QueryBuilder().search(query))
    }
    
    suspend fun getBooksByLanguages(languages: List<String>): Result<BookListResponse> {
        return client.getBooks(QueryBuilder().languages(languages))
    }
    
    suspend fun getBooksByIds(ids: List<Int>): Result<BookListResponse> {
        return client.getBooks(QueryBuilder().ids(ids))
    }
    
    suspend fun getBooksByTopic(topic: String): Result<BookListResponse> {
        return client.getBooks(QueryBuilder().topic(topic))
    }
    
    suspend fun getBooksByCopyright(copyrightStatus: Boolean?): Result<BookListResponse> {
        return client.getBooks(QueryBuilder().copyright(copyrightStatus))
    }
    
    suspend fun getBooksByAuthorYearRange(
        startYear: Int? = null,
        endYear: Int? = null
    ): Result<BookListResponse> {
        val query = QueryBuilder()
        startYear?.let { query.authorYearStart(it) }
        endYear?.let { query.authorYearEnd(it) }
        return client.getBooks(query)
    }
}

/**
 * Helper class for handling pagination operations.
 */
internal class PaginationHelper(private val apiClient: ApiClient) {
    /**
     * Get the next page of books from a BookListResponse
     */
    suspend fun getNextPage(response: BookListResponse): Result<BookListResponse> {
        return response.next?.let { nextUrl ->
            apiClient.getBooksFromUrl(nextUrl)
        } ?: Result.failure(IllegalStateException("No next page available"))
    }
    
    /**
     * Get the previous page of books from a BookListResponse
     */
    suspend fun getPreviousPage(response: BookListResponse): Result<BookListResponse> {
        return response.previous?.let { previousUrl ->
            apiClient.getBooksFromUrl(previousUrl)
        } ?: Result.failure(IllegalStateException("No previous page available"))
    }
}
