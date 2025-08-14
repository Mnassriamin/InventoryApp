package com.example.elmnassri

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        // @Volatile ensures that changes made by one thread are immediately visible to all other threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // synchronized ensures that only one thread can execute this block at a time,
            // which prevents creating multiple database instances.
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "store_database" // This will be the name of your database file
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}