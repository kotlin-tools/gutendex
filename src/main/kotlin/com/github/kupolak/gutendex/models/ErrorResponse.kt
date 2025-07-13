package com.github.kupolak.gutendex.models

import kotlinx.serialization.Serializable

/**
 * Represents an error response from the Gutendex API.
 *
 * @property detail A human-readable description of the error that occurred
 */
@Serializable
data class ErrorResponse(
    /**
     * A human-readable description of the error that occurred.
     * Examples: "No Book matches the given query.", "Invalid parameter value"
     */
    val detail: String,
) 
