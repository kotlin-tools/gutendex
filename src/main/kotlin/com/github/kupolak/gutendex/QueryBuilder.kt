package com.github.kupolak.gutendex

import java.net.URLEncoder

/**
 * A fluent builder for constructing query parameters for the Gutendex API.
 *
 * Provides methods to add various filters and sorting options to API requests.
 * All methods return the same instance to allow method chaining.
 *
 * Example usage:
 * ```kotlin
 * val query = QueryBuilder()
 *     .authorYearStart(1800)
 *     .authorYearEnd(1900)
 *     .languages(listOf("en", "fr"))
 *     .sort(SortType.POPULAR)
 * ```
 */
class QueryBuilder {
    private val params = mutableMapOf<String, String>()

    fun authorYearStart(year: Int): QueryBuilder {
        params["author_year_start"] = year.toString()
        return this
    }

    fun authorYearEnd(year: Int): QueryBuilder {
        params["author_year_end"] = year.toString()
        return this
    }

    fun copyright(status: Boolean?): QueryBuilder {
        CopyrightHelper.setCopyright(params, status)
        return this
    }

    fun copyrightMultiple(statuses: List<Boolean?>): QueryBuilder {
        CopyrightHelper.setCopyrightMultiple(params, statuses)
        return this
    }

    fun ids(ids: List<Int>): QueryBuilder {
        params["ids"] = ids.joinToString(",")
        return this
    }

    fun languages(languages: List<String>): QueryBuilder {
        params["languages"] = languages.joinToString(",")
        return this
    }

    fun mimeType(mimeType: String): QueryBuilder {
        params["mime_type"] = mimeType
        return this
    }

    fun search(query: String): QueryBuilder {
        params["search"] = query
        return this
    }

    fun sort(sort: SortType): QueryBuilder {
        params["sort"] = sort.value
        return this
    }

    fun topic(topic: String): QueryBuilder {
        params["topic"] = topic
        return this
    }

    fun buildUrl(baseUrl: String): String {
        return QueryUrlBuilder.buildUrl(baseUrl, params)
    }

    fun reset(): QueryBuilder {
        params.clear()
        return this
    }
}

/**
 * Helper class for handling copyright-related query parameters.
 */
internal object CopyrightHelper {
    fun setCopyright(
        params: MutableMap<String, String>,
        status: Boolean?,
    ) {
        params["copyright"] =
            when (status) {
                true -> "true"
                false -> "false"
                null -> "null"
            }
    }

    fun setCopyrightMultiple(
        params: MutableMap<String, String>,
        statuses: List<Boolean?>,
    ) {
        val values =
            statuses.map { status ->
                when (status) {
                    true -> "true"
                    false -> "false"
                    null -> "null"
                }
            }
        params["copyright"] = values.joinToString(",")
    }
}

/**
 * Helper class for building query URLs from parameters.
 */
internal object QueryUrlBuilder {
    private const val UTF_8 = "UTF-8"
    
    fun buildUrl(
        baseUrl: String,
        params: Map<String, String>,
    ): String {
        if (params.isEmpty()) {
            return baseUrl
        }

        val queryString = buildString {
            var first = true
            params.forEach { (key, value) ->
                if (!first) append('&')
                append(URLEncoder.encode(key, UTF_8))
                append('=')
                append(URLEncoder.encode(value, UTF_8))
                first = false
            }
        }

        return "$baseUrl?$queryString"
    }
}

/**
 * Enumeration of available sorting options for book queries.
 *
 * @property value The string value sent to the API
 */
enum class SortType(val value: String) {
    /** Sort by Project Gutenberg ID numbers from lowest to highest */
    ASCENDING("ascending"),

    /** Sort by Project Gutenberg ID numbers from highest to lowest */
    DESCENDING("descending"),

    /** Sort by popularity (download count) from most to least popular (default) */
    POPULAR("popular"),
}
