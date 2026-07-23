package com.example.medisync.ui.screens.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.repository.DocumentRepository
import com.example.medisync.model.DocumentMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class ReportsViewModel(private val repository: DocumentRepository) : ViewModel() {

    private val _documents = MutableStateFlow<List<DocumentMetadata>>(emptyList())
    val documents: StateFlow<List<DocumentMetadata>> = _documents

    init {
        FirebaseAuth.getInstance()
            .addAuthStateListener { auth ->
                val userUid = auth.currentUser?.uid
                if (userUid != null) {
                    fetchDocuments(userUid)
                } else {
                    _documents.value = emptyList()
                }
            }
    }

    fun refresh() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid != null) {
            fetchDocuments(userUid)
        }
    }

    private fun fetchDocuments(userUid: String) {
        viewModelScope.launch {
            repository.getDocuments(userUid)
                .catch { e ->
                    // Handle error
                    e.printStackTrace()
                }
                .collect { docs ->
                    _documents.value = docs
                }
        }
    }
}
