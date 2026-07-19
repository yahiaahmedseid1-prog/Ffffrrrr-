package com.example.model

import androidx.annotation.Keep

@Keep
data class Anime(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val category: String = "",
    val year: String = "",
    val status: String = "مستمر",
    val episodes: Map<String, Episode> = emptyMap()
)

@Keep
data class Episode(
    val id: String = "",
    val number: String = "",
    val title: String = "",
    val video: String = "",
    val duration: String = ""
)
