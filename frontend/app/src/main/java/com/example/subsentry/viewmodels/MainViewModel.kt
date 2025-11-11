package com.example.subsentry.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subsentry.api.RetrofitClient
import com.example.subsentry.models.Subscription
import com.example.subsentry.models.SubscriptionSummary
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _subscriptions = MutableLiveData<List<Subscription>>()
    val subscriptions: LiveData<List<Subscription>> = _subscriptions

    private val _totalAmount = MutableLiveData<Double>()
    val totalAmount: LiveData<Double> = _totalAmount

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadSubscriptions()
    }

    fun loadSubscriptions() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getSubscriptionSummary()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _subscriptions.value = body.subscriptions
                        _totalAmount.value = body.total_amount ?: 0.0
                    } else {
                        _error.value = "Получен пустой ответ от сервера"
                    }
                } else {
                    _error.value = "Ошибка загрузки данных: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка обработки данных: ${e.message}"
                e.printStackTrace() // Для отладки
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        loadSubscriptions()
    }
}