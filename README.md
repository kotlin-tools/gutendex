# Gutendex Kotlin Client

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Usage Examples](#usage-examples)
- [Code Coverage (Jacoco)](#code-coverage-jacoco)
- [License](#license)
- [Contributing](#contributing)
- [Acknowledgments](#acknowledgments)

A Kotlin client library for the [Gutendex API](https://gutendex.com/), providing access to thousands of free books from Project Gutenberg.

## Features

- 🚀 **Coroutine-based** - Fully asynchronous using Kotlin coroutines
- 🔍 **Comprehensive search** - Search by title, author, language, topic, and more
- 📖 **Complete API coverage** - All Gutendex API endpoints supported
- 🛡️ **Type-safe** - Full Kotlin type safety with data classes
- 📄 **Pagination support** - Easy navigation through result pages
- 🔧 **Flexible queries** - Builder pattern for complex queries
- 🌐 **HTTP logging** - Optional request/response logging for debugging

## Installation

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("com.github.kupolak:gutendex:1.0.0")
}
```

### Gradle (Groovy)
```groovy
dependencies {
    implementation 'com.github.kupolak:gutendex:1.0.0'
}
```

### Maven
```xml
<dependency>
    <groupId>com.github.kupolak</groupId>
    <artifactId>gutendex</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

```kotlin
import com.github.kupolak.gutendex.GutendexClient
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val client = GutendexClient()
    
    // Get popular books
    val books = client.getBooks()
    books.onSuccess { response ->
        println("Found ${response.count} books")
        response.results.forEach { book ->
            println("${book.title} by ${book.authors.joinToString { it.name }}")
        }
    }
    
    // Get a specific book
    val book = client.getBook(1342) // Pride and Prejudice
    book.onSuccess { 
        println("Title: ${it.title}")
        println("Authors: ${it.authors.joinToString { author -> author.name }}")
        println("Languages: ${it.languages.joinToString()}")
        println("Download count: ${it.downloadCount}")
    }
    
    client.close()
}
```

## Usage Examples

### Basic Search

```kotlin
val client = GutendexClient()

// Search by title and author
val searchResult = client.searchBooks("alice wonderland")
searchResult.onSuccess { response ->
    println("Found ${response.results.size} books")
}

// Get books in specific languages
val englishBooks = client.getBooksByLanguages(listOf("en"))

// Get books by topic
val childrenBooks = client.getBooksByTopic("children")
```

### Advanced Queries

```kotlin
val client = GutendexClient()

// Complex query using QueryBuilder
val result = client.getBooks(
    client.queryBuilder()
        .authorYearStart(1800)
        .authorYearEnd(1900)
        .languages(listOf("en", "fr"))
        .copyright(false) // Public domain only
        .sort(SortType.POPULAR)
)

result.onSuccess { response ->
    println("Found ${response.count} books from 19th century")
}
```

### Pagination

```kotlin
val client = GutendexClient()

var currentPage = client.getBooks()
while (currentPage.isSuccess) {
    val response = currentPage.getOrNull()!!
    
    // Process current page
    response.results.forEach { book ->
        println("${book.title} (${book.downloadCount} downloads)")
    }
    
    // Get next page
    currentPage = if (response.next != null) {
        client.getNextPage(response)
    } else {
        break
    }
}
```

### Error Handling

```kotlin
val client = GutendexClient()

val result = client.getBook(999999) // Non-existent book
result.onFailure { exception ->
    when (exception) {
        is GutendexException -> {
            println("API Error: ${exception.message} (HTTP ${exception.httpCode})")
        }
        else -> {
            println("Network Error: ${exception.message}")
        }
    }
}
```

## API Reference

### GutendexClient

The main client class for interacting with the Gutendex API.

#### Constructor
```kotlin
GutendexClient(
    baseUrl: String = "https://gutendex.com",
    enableLogging: Boolean = false
)
```

#### Methods

| Method | Description |
|--------|-------------|
| `getBooks(queryBuilder: QueryBuilder = QueryBuilder())` | Get a list of books with optional filters |
| `getBook(id: Int)` | Get a single book by its Project Gutenberg ID |
| `searchBooks(query: String)` | Search for books by title and author |
| `getBooksByLanguages(languages: List<String>)` | Get books in specific languages |
| `getBooksByIds(ids: List<Int>)` | Get books by specific Project Gutenberg IDs |
| `getBooksByTopic(topic: String)` | Get books by topic |
| `getBooksByCopyright(copyrightStatus: Boolean?)` | Get books by copyright status |
| `getBooksByAuthorYearRange(startYear: Int?, endYear: Int?)` | Get books by author year range |
| `getNextPage(response: BookListResponse)` | Get the next page of results |
| `getPreviousPage(response: BookListResponse)` | Get the previous page of results |
| `getBooksFromUrl(url: String)` | Get books from a specific URL |
| `queryBuilder()` | Create a new QueryBuilder for advanced queries |
| `close()` | Close the client and clean up resources |

### QueryBuilder

A builder class for constructing complex queries.

#### Methods

| Method | Description |
|--------|-------------|
| `authorYearStart(year: Int)` | Filter books by author birth year (minimum) |
| `authorYearEnd(year: Int)` | Filter books by author death year (maximum) |
| `copyright(status: Boolean?)` | Filter by copyright status |
| `copyrightMultiple(statuses: List<Boolean?>)` | Filter by multiple copyright statuses |
| `ids(ids: List<Int>)` | Filter by specific Project Gutenberg IDs |
| `languages(languages: List<String>)` | Filter by languages (2-character codes) |
| `mimeType(mimeType: String)` | Filter by MIME type |
| `search(query: String)` | Search in author names and book titles |
| `sort(sort: SortType)` | Sort results (ASCENDING, DESCENDING, POPULAR) |
| `topic(topic: String)` | Search in bookshelves and subjects |

### Data Classes

#### Book
```kotlin
data class Book(
    val id: Int,
    val title: String,
    val subjects: List<String>,
    val authors: List<Person>,
    val summaries: List<String>,
    val translators: List<Person>,
    val bookshelves: List<String>,
    val languages: List<String>,
    val copyright: Boolean?,
    val mediaType: String,
    val formats: Map<String, String>,
    val downloadCount: Int
)
```

#### Person
```kotlin
data class Person(
    val birthYear: Int?,
    val deathYear: Int?,
    val name: String
)
```

#### BookListResponse
```kotlin
data class BookListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Book>
)
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Acknowledgments

- [Project Gutenberg](https://www.gutenberg.org/) for providing free access to literature
- [Gutendex API](https://gutendex.com/) for the excellent API 

## Code Coverage (Jacoco)

To generate a code coverage report using Jacoco, run:

```sh
./gradlew test jacocoTestReport
```

The HTML report will be available at:

```
build/reports/jacoco/test/html/index.html
```

Open this file in your browser to view detailed test coverage for the codebase.
