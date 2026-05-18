package com.example.nallanudi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "terms")
data class Term(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val englishWord: String,
    val kannadaMeaning: String,
    val exampleSentence: String,
    val subject: String, // e.g., "Science", "Math", "Commerce"
    val isSaved: Boolean = false
)
