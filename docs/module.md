# Module Gutendex Kotlin Client

A comprehensive Kotlin client library for the [Gutendex API](https://gutendex.com/), providing access to thousands of free books from Project Gutenberg.

## Overview

The Gutendex Kotlin Client provides a type-safe, coroutine-based interface to interact with the Gutendex API. 
It supports all API endpoints and query parameters, making it easy to search, filter, and retrieve books from 
the Project Gutenberg collection.

## Key Features

- **Type Safety**: All API responses are mapped to Kotlin data classes
- **Coroutines**: Fully asynchronous using Kotlin coroutines
- **Query Builder**: Fluent API for building complex queries
- **Error Handling**: Proper handling of HTTP errors and API responses
- **Pagination**: Built-in support for paginated results
- **Logging**: Optional HTTP request/response logging for debugging

## Main Components

### GutendexClient
The main entry point for interacting with the API. Provides high-level methods for common operations.

### QueryBuilder
A fluent builder for constructing complex API queries with multiple filters and parameters.

### Data Models
- `Book`: Represents a book with all metadata
- `Person`: Represents authors and translators
- `BookListResponse`: Paginated response containing multiple books

## Usage

```kotlin
val client = GutendexClient()

// Get popular books
val books = client.getBooks()

// Search for books
val searchResults = client.searchBooks("alice wonderland")

// Advanced filtering
val filtered = client.getBooks(
    client.queryBuilder()
        .authorYearStart(1800)
        .authorYearEnd(1900)
        .languages(listOf("en"))
        .sort(SortType.POPULAR)
)

client.close()
``` 