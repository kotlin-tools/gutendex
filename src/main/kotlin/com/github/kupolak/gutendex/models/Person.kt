package com.github.kupolak.gutendex.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a person in the Gutendex system (author, translator, etc.).
 *
 * @property birthYear The year the person was born, or null if unknown
 * @property deathYear The year the person died, or null if still alive or unknown
 * @property name The full name of the person
 */
@Serializable
data class Person(
    /**
     * The year the person was born.
     * Null if the birth year is unknown.
     */
    @SerialName("birth_year")
    val birthYear: Int? = null,
    /**
     * The year the person died.
     * Null if the person is still alive or the death year is unknown.
     */
    @SerialName("death_year")
    val deathYear: Int? = null,
    /**
     * The full name of the person.
     */
    val name: String,
)
