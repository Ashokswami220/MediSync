package com.example.medisync.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.model.UserRole
import com.example.medisync.repo.DocumentRepository
import com.example.medisync.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class AdminHomeViewModel(
    private val userRepository: UserRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _totalUsers = MutableStateFlow(0)
    val totalUsers: StateFlow<Int> = _totalUsers.asStateFlow()

    private val _usersJoinedToday = MutableStateFlow(0)
    val usersJoinedToday: StateFlow<Int> = _usersJoinedToday.asStateFlow()

    private val _unclaimedPreRegUsers = MutableStateFlow(0)
    val unclaimedPreRegUsers: StateFlow<Int> = _unclaimedPreRegUsers.asStateFlow()

    private val _reportsOpenedCount = MutableStateFlow(0L)
    val reportsOpenedCount: StateFlow<Long> = _reportsOpenedCount.asStateFlow()

    private val _reportsOpenedTodayCount = MutableStateFlow(0L)
    val reportsOpenedTodayCount: StateFlow<Long> = _reportsOpenedTodayCount.asStateFlow()

    private val _totalUploadedReportsCount = MutableStateFlow(0L)
    val totalUploadedReportsCount: StateFlow<Long> = _totalUploadedReportsCount.asStateFlow()

    init {
        fetchUsersStats()
        fetchReportsStats()
    }

    private fun fetchUsersStats() {
        viewModelScope.launch {
            userRepository.getAllUsers()
                .catch { /* ignore */ }
                .collectLatest { profiles ->
                    _totalUsers.value =
                        profiles.count { it.role != UserRole.ADMIN && !it.isPlaceholder }

                    // Calculate joined today (only real users)
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startOfToday = calendar.timeInMillis

                    _usersJoinedToday.value = profiles.count {
                        it.role != UserRole.ADMIN && !it.isPlaceholder && it.accountCreatedTime >= startOfToday
                    }

                    _unclaimedPreRegUsers.value = profiles.count {
                        it.isPlaceholder && it.claimedByUid.isNullOrEmpty()
                    }
                }
        }
    }

    private fun fetchReportsStats() {
        viewModelScope.launch {
            documentRepository.getReportOpenCount()
                .catch { /* ignore */ }
                .collectLatest { stats ->
                    _reportsOpenedCount.value = stats.totalOpened
                    _reportsOpenedTodayCount.value = stats.todayOpened
                }
        }

        viewModelScope.launch {
            documentRepository.getTotalReportsCount()
                .catch { /* ignore */ }
                .collectLatest { count ->
                    _totalUploadedReportsCount.value = count
                }
        }
    }
}
