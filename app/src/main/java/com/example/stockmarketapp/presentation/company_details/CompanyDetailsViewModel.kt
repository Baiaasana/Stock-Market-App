package com.example.stockmarketapp.presentation.company_details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmarketapp.domain.repository.StockRepository
import com.example.stockmarketapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: StockRepository
) : ViewModel() {

    var state by mutableStateOf(CompanyDetailsState())

    init {
        viewModelScope.launch {
            val symbol = savedStateHandle.get<String>("symbol") ?: return@launch
            state = state.copy(isLoading = true)
            val companyDetailsResult = async { repository.getCompanyDetails(symbol) }
            val intraDayInfoResult = async { repository.getIntraDayInfo(symbol) }
            when (val result = companyDetailsResult.await()) {
                is Resource.Success -> {
                    state = state.copy(
                        company = result.data,
                        isLoading = false,
                        error = null,
                    )
                }

                is Resource.Error -> {
                    state = state.copy(
                        company = null,
                        isLoading = false,
                        error = result.message.toString()
                    )
                }

                else -> Unit
            }

            when (val result = intraDayInfoResult.await()) {
                is Resource.Success -> {
                    state = state.copy(
                        stocks = result.data ?: emptyList(),
                        isLoading = false,
                        error = null,
                    )
                }

                is Resource.Error -> {
                    state = state.copy(
                        company = null,
                        isLoading = false,
                        error = result.message.toString()
                    )
                }

                else -> Unit
            }
        }
    }
}