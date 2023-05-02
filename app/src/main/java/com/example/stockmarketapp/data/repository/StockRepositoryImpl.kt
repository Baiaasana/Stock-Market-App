package com.example.stockmarketapp.data.repository

import android.util.Log
import com.example.stockmarketapp.data.csv.CSVParser
import com.example.stockmarketapp.data.locale.StockDatabase
import com.example.stockmarketapp.data.mapper.toCompanyDetails
import com.example.stockmarketapp.data.mapper.toCompanyListing
import com.example.stockmarketapp.data.mapper.toCompanyListingEntity
import com.example.stockmarketapp.data.remote.StockApi
import com.example.stockmarketapp.domain.model.CompanyDetails
import com.example.stockmarketapp.domain.model.CompanyListing
import com.example.stockmarketapp.domain.model.IntraDayInfo
import com.example.stockmarketapp.domain.repository.StockRepository
import com.example.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val database: StockDatabase,
    private val companyListingParser: CSVParser<CompanyListing>,
    private val intraDayParser: CSVParser<IntraDayInfo>
) : StockRepository {

    private val dao = database.dao
    override suspend fun getCompanyListing(
        fetchFromRemote: Boolean, query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(true))
            val localListing = dao.searchCompanyByName(query)
            emit(Resource.Success(data = localListing.map { it.toCompanyListing() }))
            Log.d("log", "localListing impl ".plus(localListing))

            val isDbEmpty = localListing.isEmpty() && query.isBlank()
            val justLoadFromCache = !isDbEmpty && !fetchFromRemote
            if (justLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteListing = try {
                val response = api.getListings()
                Log.d("log", "response impl ".plus(response.byteStream()))
                companyListingParser.parse(response.byteStream())
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }
            Log.d("log", "remoteListing impl ".plus(remoteListing))
            remoteListing?.let { listings ->
                dao.clearListing()
                dao.insertListings(listings.map { it.toCompanyListingEntity() })
                emit(
                    Resource.Success(data = dao.searchCompanyByName("")
                        .map { it.toCompanyListing() })
                )
                val data = dao.searchCompanyByName("").map { it.toCompanyListing() }
                Log.d("log", "data impl ".plus(data))

                emit(Resource.Loading(false))
            }
        }
    }

    override suspend fun getIntraDayInfo(symbol: String): Resource<List<IntraDayInfo>> {
        return try {
            val response = api.getIntraDayInfo(symbol = symbol)
            val result = intraDayParser.parse(response.byteStream())
            Resource.Success(result)
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.Error(message = "Couldn't load IntraDay info")

        } catch (e: HttpException) {
            e.printStackTrace()
            Resource.Error(message = "Couldn't load IntraDay info")
        }
    }

    override suspend fun getCompanyDetails(symbol: String): Resource<CompanyDetails> {
        return try {
            val result = api.getDetailedInfo(symbol = symbol)
            Resource.Success(result.toCompanyDetails())
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.Error(message = "Couldn't load company info")

        } catch (e: HttpException) {
            e.printStackTrace()
            Resource.Error(message = "Couldn't load company info")
        }
    }
}