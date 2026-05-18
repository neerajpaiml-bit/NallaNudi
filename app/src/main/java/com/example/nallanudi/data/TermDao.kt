package com.example.nallanudi.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TermDao {
    @Query("SELECT * FROM terms")
    fun getAllTerms(): Flow<List<Term>>

    @Query("SELECT * FROM terms WHERE englishWord LIKE :searchQuery OR kannadaMeaning LIKE :searchQuery")
    fun searchTerms(searchQuery: String): Flow<List<Term>>

    @Query("SELECT * FROM terms WHERE subject = :subject")
    fun getTermsBySubject(subject: String): Flow<List<Term>>

    @Query("SELECT * FROM terms WHERE isSaved = 1")
    fun getSavedTerms(): Flow<List<Term>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerm(term: Term)

    @Update
    suspend fun updateTerm(term: Term)

    @Query("UPDATE terms SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isSaved: Boolean)
}
