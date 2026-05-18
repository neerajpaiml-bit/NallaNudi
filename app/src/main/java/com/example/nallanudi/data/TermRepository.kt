package com.example.nallanudi.data

import kotlinx.coroutines.flow.Flow

class TermRepository(private val termDao: TermDao) {
    val allTerms: Flow<List<Term>> = termDao.getAllTerms()
    val savedTerms: Flow<List<Term>> = termDao.getSavedTerms()

    fun searchTerms(query: String): Flow<List<Term>> {
        return termDao.searchTerms("%$query%")
    }

    fun getTermsBySubject(subject: String): Flow<List<Term>> {
        return termDao.getTermsBySubject(subject)
    }

    suspend fun updateFavoriteStatus(id: Int, isSaved: Boolean) {
        termDao.updateFavoriteStatus(id, isSaved)
    }
}
