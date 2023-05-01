package com.example.stockmarketapp.data.repository

import android.util.Log
import com.example.stockmarketapp.data.csv.CSVParser
import com.example.stockmarketapp.data.locale.StockDatabase
import com.example.stockmarketapp.data.mapper.toCompanyListing
import com.example.stockmarketapp.data.mapper.toCompanyListingEntity
import com.example.stockmarketapp.data.remote.StockApi
import com.example.stockmarketapp.domain.model.CompanyListing
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
                    Resource.Success(
                        data = dao.searchCompanyByName("").map { it.toCompanyListing() })
                )
                val data = dao.searchCompanyByName("").map { it.toCompanyListing() }
                Log.d("log", "data impl ".plus(data))

                emit(Resource.Loading(false))
            }
        }
    }
}