package com.example.stockmarketapp.data.repository

import com.example.stockmarketapp.data.csv.CSVParser
import com.example.stockmarketapp.data.csv.CompanyListingParser
import com.example.stockmarketapp.data.locale.StockDatabase
import com.example.stockmarketapp.data.mapper.toCompanyListing
import com.example.stockmarketapp.data.mapper.toCompanyListingEntity
import com.example.stockmarketapp.data.remote.StockApi
import com.example.stockmarketapp.domain.StockRepository
import com.example.stockmarketapp.domain.model.CompanyListing
import com.example.stockmarketapp.util.Resource
import com.opencsv.CSVReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val database: StockDatabase,
    private val companyListingParser: CSVParser<CompanyListing>
) : StockRepository {

    private val dao = database.dao
    override suspend fun getCompanyListing(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(true))
            val localListing = dao.searchCompanyByName(query)
            emit(Resource.Success(data = localListing.map { it.toCompanyListing() }))

            val isDbEmpty = localListing.isEmpty() && query.isBlank()
            val justLoadFromCache = !isDbEmpty && !fetchFromRemote
            if (justLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteListing = try {
                val response = api.getListings()
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
            remoteListing?.let { listings ->
                dao.clearListing()
                dao.insertListings(listings.map { it.toCompanyListingEntity() })
//                emit(Resource.Success(listings))
                emit(Resource.Success(data = dao.searchCompanyByName("").map { it.toCompanyListing() }))
                emit(Resource.Loading(false))
            }
        }
    }
}