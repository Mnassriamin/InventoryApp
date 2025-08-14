package com.example.elmnassri

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items_table")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "item_name")
    val name: String = "", // Add default value

    @ColumnInfo(name = "item_price")
    val price: Double = 0.0, // Add default value

    @ColumnInfo(name = "barcode_data")
    val barcode: String = "", // Add default value

    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null
)