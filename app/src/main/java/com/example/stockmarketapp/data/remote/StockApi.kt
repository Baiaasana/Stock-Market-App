package com.example.stockmarketapp.data.remote

import com.example.stockmarketapp.data.remote.dto.CompanyDetailsDto
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
interface StockApi {
    @GET("query?function=LISTING_STATUS")
    suspend fun getListings(
        @Query("apikey") apiKey: String = API_KEY
    ): ResponseBody

    @GET("query?function=TIME_SERIES_INTRADAY&interval=60min&datatype=csv")
    suspend fun getIntraDayInfo(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = API_KEY
    ): ResponseBody

    @GET("query?function=OVERVIEW")
    suspend fun getDetailedInfo(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = API_KEY
    ): CompanyDetailsDto

    companion object {
        const val API_KEY = "1X78D1M4M94KCCW7"
        const val BASE_URL = "https://alphavantage.co"
    }
}