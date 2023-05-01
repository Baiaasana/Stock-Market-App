package com.example.stockmarketapp.presentation.company_listing

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmarketapp.domain.repository.StockRepository
import com.example.stockmarketapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyListingsViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    var state by mutableStateOf(CompanyListingsState())
    private var searchJob: Job? = null

    init {
        getCompanyListings()
    }

    fun onEvent(event: CompanyListingsEvent) {
        when (event) {
            is CompanyListingsEvent.OnSearchQueryChange -> {
                state = state.copy(searchQuery = event.query)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getCompanyListings()
                }
                Log.d("log", "search")

            }

            is CompanyListingsEvent.Refresh -> {
                getCompanyListings(fetchFromRemote = true)
                Log.d("log", "swipe")

            }
        }
    }

    private fun getCompanyListings(
        query: String = state.searchQuery.lowercase(),
        fetchFromRemote: Boolean = false
    ) {
        Log.d("log", "get")
        viewModelScope.launch {
            repository.getCompanyListing(fetchFromRemote, query)
                .collect { result ->
                    when (result) {
                        is Resource.Error -> Unit
                        is Resource.Loading -> state = state.copy(isLoading = result.loading)
                        is Resource.Success -> {
                            result.data?.let { listings ->
                                state = state.copy(companies = listings)
                                Log.d("log", "succes vm ")
                            }
                        }
                    }
                }
        }
    }
}
