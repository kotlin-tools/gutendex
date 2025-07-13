package com.github.kupolak.gutendex.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a book from the Gutendex API.
 * Contains all metadata and information about a Project Gutenberg book.
 *
 * @property id The unique Project Gutenberg ID number for this book
 * @property title The title of the book
 * @property subjects List of subject classifications for the book
 * @property authors List of authors who wrote this book
 * @property summaries List of automatically generated summaries
 * @property translators List of people who translated this book
 * @property bookshelves List of bookshelves this book belongs to
 * @property languages List of language codes (e.g., "en", "fr") for the book
 * @property copyright Copyright status: true for copyrighted, false for public domain, null for unknown
 * @property mediaType The MIME type category (e.g., "Text")
 * @property formats Map of MIME types to download URLs for different formats
 * @property downloadCount Number of times this book has been downloaded from Project Gutenberg
 */
@Serializable
data class Book(
    /**
     * The unique Project Gutenberg ID number for this book.
     */
    val id: Int,
    /**
     * The title of the book.
     */
    val title: String,
    /**
     * List of subject classifications for the book.
     * These are typically Library of Congress subject headings.
     */
    val subjects: List<String>,
    /**
     * List of authors who wrote this book.
     */
    val authors: List<Person>,
    /**
     * List of automatically generated summaries for the book.
     */
    val summaries: List<String>,
    /**
     * List of people who translated this book.
     */
    val translators: List<Person>,
    /**
     * List of Project Gutenberg bookshelves this book belongs to.
     */
    val bookshelves: List<String>,
    /**
     * List of language codes (e.g., "en", "fr") for the book.
     */
    val languages: List<String>,
    /**
     * Copyright status of the book.
     * - true: Book is under copyright
     * - false: Book is in the public domain
     * - null: Copyright status is unknown
     */
    val copyright: Boolean? = null,
    /**
     * The MIME type category for this book (e.g., "Text").
     */
    @SerialName("media_type")
    val mediaType: String,
    /**
     * Map of MIME types to download URLs for different formats.
     * Keys are MIME types like "text/html", "application/epub+zip", etc.
     * Values are URLs where the book can be downloaded in that format.
     */
    val formats: Map<String, String>,
    /**
     * Number of times this book has been downloaded from Project Gutenberg.
     * Used as a popularity metric.
     */
    @SerialName("download_count")
    val downloadCount: Int,
)
