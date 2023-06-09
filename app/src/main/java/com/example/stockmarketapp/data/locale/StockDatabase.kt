package com.example.stockmarketapp.data.locale

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CompanyListingEntity::class],
    exportSchema = false,
    version = 1,
)
abstract class StockDatabase : RoomDatabase() {

    abstract val dao: StockDao
}