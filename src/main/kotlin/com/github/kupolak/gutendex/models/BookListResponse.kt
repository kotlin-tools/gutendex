package com.github.kupolak.gutendex.models

import kotlinx.serialization.Serializable

/**
 * Represents a paginated response from the Gutendex API containing a list of books.
 *
 * @property count The total number of books matching the query across all pages
 * @property next URL to the next page of results, or null if this is the last page
 * @property previous URL to the previous page of results, or null if this is the first page
 * @property results The list of books on this page (0-32 books per page)
 */
@Serializable
data class BookListResponse(
    /**
     * The total number of books matching the query across all pages.
     */
    val count: Int,
    /**
     * URL to the next page of results.
     * Null if this is the last page.
     */
    val next: String? = null,
    /**
     * URL to the previous page of results.
     * Null if this is the first page.
     */
    val previous: String? = null,
    /**
     * The list of books on this page.
     * Each page contains 0-32 books.
     */
    val results: List<Book>,
) 
