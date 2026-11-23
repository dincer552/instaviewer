package com.dincer552.instaviewer.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    protected fun setLoading(value: Boolean) {
        viewModelScope.launch { _loading.value = value }
    }

    protected fun setError(message: String?) {
        viewModelScope.launch { _error.value = message }
    }

    protected fun clearError() {
        viewModelScope.launch { _error.value = null }
    }
}
