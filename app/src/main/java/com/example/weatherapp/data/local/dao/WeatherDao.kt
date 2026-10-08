package com.example.weatherapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.data.local.entity.WeatherEntity

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWeather(weather: WeatherEntity)

    @Query("SELECT * FROM weather WHERE id = 1 LIMIT 1")
    suspend fun getLatestWeather(): WeatherEntity?

    @Query("DELETE FROM weather")
    suspend fun clearWeather()
}