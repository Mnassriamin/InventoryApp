package com.example.elmnassri

import android.app.Application

class InventoryApplication : Application() {
    // Using 'lazy' so the database and repository are only created when they're needed
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { ItemRepository(database.itemDao()) }
}