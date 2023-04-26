package com.example.stockmarketapp.data.csv

import java.io.InputStream
import java.util.stream.IntStream

interface CSVParser <T> {
    suspend fun  parse(stream: InputStream): List<T>
}