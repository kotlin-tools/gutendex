@file:Suppress("ktlint:standard:argument-list-wrapping")
package com.github.kupolak.gutendex

import kotlinx.coroutines.runBlocking
import com.github.kupolak.gutendex.models.Book

private const val SAMPLE_BOOKS_COUNT = 3
private const val PRIDE_AND_PREJUDICE_ID = 1342
private const val AUTHOR_YEAR_START = 1800
private const val AUTHOR_YEAR_END = 1900
private const val NON_EXISTENT_BOOK_ID = 999999

/**
 * Example usage of the Gutendex Kotlin client
 */
fun main() = runBlocking {
    val client = GutendexClient(enableLogging = true)
    
    println("=== Gutendex Kotlin Client Demo ===\n")
    
    demonstratePopularBooks(client)
    demonstrateSpecificBook(client)
    demonstrateSearch(client)
    demonstrateLanguageFilter(client)
    demonstrateAdvancedQuery(client)
    demonstrateTopicSearch(client)
    demonstratePagination(client)
    demonstrateErrorHandling(client)
    
    println("\n=== Demo Complete ===")
    client.close()
}

private suspend fun demonstratePopularBooks(client: GutendexClient) {
    println("1. Getting popular books...")
    val popularBooks = client.getBooks()
    popularBooks.onSuccess { response ->
        println("✅ Found ${response.count} total books")
        println("📚 Showing first ${response.results.size} books:")
        response.results.take(SAMPLE_BOOKS_COUNT).forEach { book ->
            println(
                "   • ${book.title} by ${book.authors.joinToString { it.name }} " +
                    "(${book.downloadCount} downloads)",
            )
        }
    }.onFailure { exception ->
        println("❌ Error: ${exception.message}")
    }
    println()
}

private suspend fun demonstrateSpecificBook(client: GutendexClient) {
    println("2. Getting Pride and Prejudice (ID: $PRIDE_AND_PREJUDICE_ID)...")
    val book = client.getBook(PRIDE_AND_PREJUDICE_ID)
    book.onSuccess { 
        println("✅ Title: ${it.title}")
        println("📖 Authors: ${it.authors.joinToString { author -> author.name }}")
        println("🌍 Languages: ${it.languages.joinToString()}")
        println("📥 Download count: ${it.downloadCount}")
        println("📚 Subjects: ${it.subjects.take(SAMPLE_BOOKS_COUNT).joinToString(", ")}")
    }.onFailure { exception ->
        println("❌ Error: ${exception.message}")
    }
    println()
}

private suspend fun demonstrateSearch(client: GutendexClient) {
    println("3. Searching for 'Alice' books...")
    val searchResult = client.searchBooks("alice")
    searchResult.onSuccess { response ->
        println("✅ Found ${response.results.size} books matching 'alice':")
        response.results.take(SAMPLE_BOOKS_COUNT).forEach { book ->
            println(
                "   • ${book.title} by ${book.authors.joinToString { it.name }}",
            )
        }
    }.onFailure { exception ->
        println("❌ Error: ${exception.message}")
    }
    println()
}

private suspend fun demonstrateLanguageFilter(client: GutendexClient) {
    println("4. Getting books in English and French...")
    val languageBooks = client.getBooksByLanguages(listOf("en", "fr"))
    languageBooks.onSuccess { response ->
        println("✅ Found ${response.count} books in English and French")
        println("📚 Sample books:")
        response.results.take(SAMPLE_BOOKS_COUNT).forEach { book ->
            println(
                "   • ${book.title} [${book.languages.joinToString()}]",
            )
        }
    }.onFailure { exception ->
        println("❌ Error: ${exception.message}")
    }
    println()
}

private suspend fun demonstrateAdvancedQuery(client: GutendexClient) {
    println("5. Advanced query: 19th century English books...")
    val advancedQuery = client.queryBuilder()
        .authorYearStart(AUTHOR_YEAR_START)
        .authorYearEnd(AUTHOR_YEAR_END)
        .languages(listOf("en"))
        .copyright(false) // Public domain only
        .sort(SortType.POPULAR)
    val advancedResult = client.getBooks(advancedQuery)
    advancedResult.onSuccess { response ->
        println("✅ Found  ${response.count} 19th century English books")
        println("📚 Top $SAMPLE_BOOKS_COUNT by popularity:")
        response.results.take(SAMPLE_BOOKS_COUNT).forEach { book ->
            printBookWithAuthorYears(book)
        }
    }.onFailure { exception ->
        println("❌ Error: ${exception.message}")
    }
    println()
}

private fun printBookWithAuthorYears(book: Book) {
    val author = book.authors.firstOrNull()
    val birthYear = author?.birthYear?.let { "($it" } ?: ""
    val deathYear = author?.deathYear?.let { "-$it)" } ?: if (birthYear.isNotEmpty()) ")" else ""
    println(
        "   • ${book.title} by ${book.authors.joinToString { author -> author.name }} " +
            "$birthYear$deathYear",
    )
    println("     Downloads: ${book.downloadCount}")
}

private suspend fun demonstrateTopicSearch(client: GutendexClient) {
    println("6. Getting children's books...")
    val childrenBooks = client.getBooksByTopic("children")
    childrenBooks.onSuccess { response ->
        println("✅ Found ${response.count} children's books")
        println("📚 Sample titles:")
        response.results.take(SAMPLE_BOOKS_COUNT).forEach { book ->
            println("   • ${book.title}")
        }
    }.onFailure { exception ->
        println("❌ Error: ${exception.message}")
    }
    println()
}

private suspend fun demonstratePagination(client: GutendexClient) {
    println("7. Pagination example...")
    val firstPage = client.getBooks(
        client.queryBuilder()
            .languages(listOf("en"))
            .sort(SortType.POPULAR),
    )
    firstPage.onSuccess { response ->
        println("✅ First page: ${response.results.size} books")
        if (response.next != null) {
            println("🔄 Getting next page...")
            val nextPage = client.getNextPage(response)
            nextPage.onSuccess { nextResponse ->
                println("✅ Next page: ${nextResponse.results.size} books")
                println(
                    "📚 First book from next page: ${nextResponse.results.firstOrNull()?.title}",
                )
            }
        } else {
            println("ℹ️  No next page available")
        }
    }.onFailure { exception ->
        println("❌ Error: ${exception.message}")
    }
    println()
}

private suspend fun demonstrateErrorHandling(client: GutendexClient) {
    println("8. Error handling example (non-existent book)...")
    val errorResult = client.getBook(NON_EXISTENT_BOOK_ID)
    errorResult.onFailure { exception ->
        when (exception) {
            is GutendexException -> {
                println(
                    "✅ Handled API Error: ${exception.message} (HTTP ${exception.httpCode})",
                )
            }
            else -> {
                println("❌ Network Error: ${exception.message}")
            }
        }
    }.onSuccess {
        println("🤔 Unexpected success for non-existent book")
    }
}
