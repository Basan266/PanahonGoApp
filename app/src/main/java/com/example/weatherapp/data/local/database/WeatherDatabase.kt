package com.example.weatherapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.weatherapp.data.local.dao.WeatherDao
import com.example.weatherapp.data.local.entity.WeatherEntity

@Database(
    entities = [
        WeatherEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WeatherDatabase :
    RoomDatabase() {

    abstract fun weatherDao(): WeatherDao

    companion object {

        @Volatile
        private var instance:
                WeatherDatabase? = null

        private val migrationFrom1To2 =
            object : Migration(
                startVersion = 1,
                endVersion = 2
            ) {
                override fun migrate(
                    db: SupportSQLiteDatabase
                ) {
                    db.execSQL(
                        """
                        ALTER TABLE weather
                        ADD COLUMN conditionType TEXT NOT NULL DEFAULT ''
                        """.trimIndent()
                    )

                    db.execSQL(
                        """
                        ALTER TABLE weather
                        ADD COLUMN conditionDescription TEXT NOT NULL DEFAULT ''
                        """.trimIndent()
                    )

                    db.execSQL(
                        """
                        ALTER TABLE weather
                        ADD COLUMN iconBaseUri TEXT NOT NULL DEFAULT ''
                        """.trimIndent()
                    )
                }
            }

        fun getInstance(
            context: Context
        ): WeatherDatabase {
            return instance
                ?: synchronized(this) {
                    instance
                        ?: Room.databaseBuilder(
                            context.applicationContext,
                            WeatherDatabase::class.java,
                            "weather_database"
                        )
                            .addMigrations(
                                migrationFrom1To2
                            )
                            .build()
                            .also {
                                instance = it
                            }
                }
        }
    }
}