package com.example.stockmarketapp.data.mapper

import com.example.stockmarketapp.data.locale.CompanyListingEntity
import com.example.stockmarketapp.data.remote.dto.CompanyDetailsDto
import com.example.stockmarketapp.domain.model.CompanyDetails
import com.example.stockmarketapp.domain.model.CompanyListing

fun CompanyListingEntity.toCompanyListing(): CompanyListing {
    return CompanyListing(
        name = name, symbol = symbol, exchange = exchange
    )
}

fun CompanyListing.toCompanyListingEntity(): CompanyListingEntity {
    return CompanyListingEntity(
        name = name, symbol = symbol, exchange = exchange
    )
}

fun CompanyDetailsDto.toCompanyDetails(): CompanyDetails {
    return CompanyDetails(
        symbol = symbol ?: "",
        description = description ?: "",
        name = name ?: "",
        country = country ?: "",
        industry = industry ?: "",
        )
}