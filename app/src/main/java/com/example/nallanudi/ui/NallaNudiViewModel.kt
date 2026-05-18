package com.example.nallanudi.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nallanudi.data.AppDatabase
import com.example.nallanudi.data.Term
import com.example.nallanudi.data.TermRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NallaNudiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TermRepository
    val allTerms: StateFlow<List<Term>>
    val savedTerms: StateFlow<List<Term>>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedSubject = MutableStateFlow<String?>(null)
    val selectedSubject = _selectedSubject.asStateFlow()

    val filteredTerms: StateFlow<List<Term>>
    val wordOfTheDay: StateFlow<Term?>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = TermRepository(database.termDao())
        
        allTerms = repository.allTerms.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        savedTerms = repository.savedTerms.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredTerms = combine(_searchQuery, _selectedSubject, allTerms) { query, subject, terms ->
            terms.filter { term ->
                val matchesQuery = term.englishWord.contains(query, ignoreCase = true) ||
                        term.kannadaMeaning.contains(query, ignoreCase = true)
                val matchesSubject = subject == null || term.subject.equals(subject, ignoreCase = true)
                matchesQuery && matchesSubject
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        wordOfTheDay = allTerms.map { terms ->
            if (terms.isNotEmpty()) terms.first() else null
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSubjectFilterChange(subject: String?) {
        _selectedSubject.value = if (_selectedSubject.value == subject) null else subject
    }

    fun toggleSaveTerm(term: Term) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(term.id, !term.isSaved)
        }
    }
}
