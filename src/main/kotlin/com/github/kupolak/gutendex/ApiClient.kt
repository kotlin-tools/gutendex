package com.github.kupolak.gutendex

import com.github.kupolak.gutendex.models.Book
import com.github.kupolak.gutendex.models.BookListResponse
import com.github.kupolak.gutendex.models.ErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val DEFAULT_TIMEOUT_SECONDS: Long = 30L

/**
 * Low-level HTTP client for the Gutendex API.
 *
 * Handles HTTP requests, response parsing, and error handling.
 * Uses OkHttp for HTTP operations and kotlinx.serialization for JSON parsing.
 *
 * @param baseUrl The base URL for the API (default: https://gutendex.com)
 * @param enableLogging Whether to enable HTTP request/response logging for debugging
 */
class ApiClient(
    private val baseUrl: String = "https://gutendex.com",
    private val enableLogging: Boolean = false,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    private val httpClient: OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply {
                if (enableLogging) {
                    val logging =
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    addInterceptor(logging)
                }
            }
            .build()

    /**
     * Retrieves a list of books from the API based on the provided query parameters.
     *
     * @param query The query builder containing filters and parameters for the request
     * @return Result containing BookListResponse on success or Exception on failure
     */
    suspend fun getBooks(query: QueryBuilder = QueryBuilder()): Result<BookListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val url = query.buildUrl("$baseUrl/books")
                val request =
                    Request.Builder()
                        .url(url)
                        .get()
                        .build()

                httpClient.newCall(request).execute().use { response ->
                    val body =
                        response.body?.string()
                            ?: return@withContext Result.failure(IOException("Empty response body"))

                    if (response.isSuccessful) {
                        val books = json.decodeFromString<BookListResponse>(body)
                        Result.success(books)
                    } else {
                        var error: ErrorResponse
                        var parseException: SerializationException?
                        try {
                            error = json.decodeFromString<ErrorResponse>(body)
                            parseException = null
                        } catch (e: SerializationException) {
                            error = ErrorResponse("HTTP ${response.code}: ${response.message}")
                            parseException = e
                        }
                        Result.failure(GutendexException(error.detail, response.code, parseException))
                    }
                }
            } catch (e: IOException) {
                Result.failure(e)
            } catch (e: SerializationException) {
                Result.failure(e)
            }
        }
    }

    /**
     * Retrieves a single book by its Project Gutenberg ID.
     *
     * @param id The unique Project Gutenberg ID of the book
     * @return Result containing Book on success or Exception on failure
     */
    suspend fun getBook(id: Int): Result<Book> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$baseUrl/books/$id"
                val request =
                    Request.Builder()
                        .url(url)
                        .get()
                        .build()

                httpClient.newCall(request).execute().use { response ->
                    val body =
                        response.body?.string()
                            ?: return@withContext Result.failure(IOException("Empty response body"))

                    if (response.isSuccessful) {
                        val book = json.decodeFromString<Book>(body)
                        Result.success(book)
                    } else {
                        var error: ErrorResponse
                        var parseException: SerializationException?
                        try {
                            error = json.decodeFromString<ErrorResponse>(body)
                            parseException = null
                        } catch (e: SerializationException) {
                            error = ErrorResponse("HTTP ${response.code}: ${response.message}")
                            parseException = e
                        }
                        Result.failure(GutendexException(error.detail, response.code, parseException))
                    }
                }
            } catch (e: IOException) {
                Result.failure(e)
            } catch (e: SerializationException) {
                Result.failure(e)
            }
        }
    }

    /**
     * Retrieves books from a specific URL.
     *
     * This method is particularly useful for pagination, where you need to fetch
     * the next or previous page using URLs provided in the API response.
     *
     * @param url The complete URL to fetch books from
     * @return Result containing BookListResponse on success or Exception on failure
     */
    suspend fun getBooksFromUrl(url: String): Result<BookListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request =
                    Request.Builder()
                        .url(url)
                        .get()
                        .build()

                httpClient.newCall(request).execute().use { response ->
                    val body =
                        response.body?.string()
                            ?: return@withContext Result.failure(IOException("Empty response body"))

                    if (response.isSuccessful) {
                        val books = json.decodeFromString<BookListResponse>(body)
                        Result.success(books)
                    } else {
                        var error: ErrorResponse
                        var parseException: SerializationException?
                        try {
                            error = json.decodeFromString<ErrorResponse>(body)
                            parseException = null
                        } catch (e: SerializationException) {
                            error = ErrorResponse("HTTP ${response.code}: ${response.message}")
                            parseException = e
                        }
                        Result.failure(GutendexException(error.detail, response.code, parseException))
                    }
                }
            } catch (e: IOException) {
                Result.failure(e)
            } catch (e: SerializationException) {
                Result.failure(e)
            }
        }
    }

    /**
     * Closes the HTTP client and releases all resources.
     *
     * This method should be called when you're done using the ApiClient
     * to ensure proper cleanup of connections and threads.
     */
    fun close() {
        httpClient.dispatcher.executorService.shutdown()
        httpClient.connectionPool.evictAll()
    }
}

/**
 * Exception thrown when the Gutendex API returns an error response.
 *
 * This exception contains both the error message from the API and the HTTP status code
 * to help with debugging and error handling.
 *
 * @property message The error message from the API
 * @property httpCode The HTTP status code of the failed request
 * @property cause The original exception that caused this error, if any
 */
class GutendexException(
    message: String,
    val httpCode: Int,
    override val cause: Throwable? = null
) : Exception(message, cause)
