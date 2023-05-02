package com.example.stockmarketapp.presentation.company_details

import com.example.stockmarketapp.domain.model.CompanyDetails
import com.example.stockmarketapp.domain.model.IntraDayInfo

data class CompanyDetailsState(
    val stocks: List<IntraDayInfo> = emptyList(),
    val company: CompanyDetails? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
