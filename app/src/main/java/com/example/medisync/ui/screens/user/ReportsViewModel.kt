package com.example.medisync.ui.screens.user

import kotlinx.coroutines.Job

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.repo.DocumentRepository
import com.example.medisync.model.DocumentMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.example.medisync.repo.AuthRepository

sealed class ReportsState {
    object Loading : ReportsState()
    data class Success(val documents: List<DocumentMetadata>) : ReportsState()
    object Empty : ReportsState()
    data class Error(val message: String) : ReportsState()
}

class ReportsViewModel(
    private val repository: DocumentRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _reportsState = MutableStateFlow<ReportsState>(ReportsState.Loading)
    val reportsState: StateFlow<ReportsState> = _reportsState

    private var fetchJob: Job? = null

    fun loadDocuments() {
        val userUid = authRepo.getCurrentUserUid()
        if (userUid != null) {
            fetchDocuments(userUid, showLoading = true)
        } else {
            _reportsState.value = ReportsState.Error("User not logged in")
        }
    }

    fun refresh() {
        val userUid = authRepo.getCurrentUserUid()
        if (userUid != null) {
            fetchDocuments(userUid, showLoading = false)
        }
    }

    private fun fetchDocuments(userUid: String, showLoading: Boolean) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            if (showLoading && _reportsState.value !is ReportsState.Success && _reportsState.value !is ReportsState.Empty) {
                _reportsState.value = ReportsState.Loading
            }
            repository.getDocuments(userUid)
                .catch { e ->
                    _reportsState.value = ReportsState.Error(e.message ?: "Failed to load documents")
                }
                .collect { docs ->
                    if (docs.isEmpty()) {
                        _reportsState.value = ReportsState.Empty
                    } else {
                        _reportsState.value = ReportsState.Success(docs)
                    }
                }
        }
    }
}
