package com.antigravity.telemetry.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [VehicleEntity::class, FuelEventEntity::class, DriveSegmentEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelEventDao(): FuelEventDao
    abstract fun driveSegmentDao(): DriveSegmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "antigravity_telemetry.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getInstance(context))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(database: AppDatabase) {
            // Clean baseline: only the vehicle profile, zero fake events
            val vehicle = VehicleEntity(
                id = "default-vehicle-victoris",
                name = "Victoris CNG",
                cngTankCapacityKg = 10.0,
                petrolTankCapacityL = 45.0,
                estimatedWarmupDistanceKmPerColdStart = 1.2,
                activeOdometerKm = 0.0
            )
            database.vehicleDao().upsertVehicle(vehicle)
        }

        suspend fun resetAllActualData(database: AppDatabase) {
            database.fuelEventDao().deleteAllEvents(isSimulation = false)
            database.driveSegmentDao().deleteAllSegments(isSimulation = false)
            val vehicle = VehicleEntity(
                id = "default-vehicle-victoris",
                name = "Victoris CNG",
                cngTankCapacityKg = 10.0,
                petrolTankCapacityL = 45.0,
                estimatedWarmupDistanceKmPerColdStart = 1.2,
                activeOdometerKm = 0.0
            )
            database.vehicleDao().upsertVehicle(vehicle)
        }
    }
}
