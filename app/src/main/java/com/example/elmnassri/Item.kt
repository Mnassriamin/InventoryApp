package com.example.elmnassri

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items_table")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "item_name")
    val name: String,

    @ColumnInfo(name = "item_price")
    val price: Double,

    @ColumnInfo(name = "barcode_data")
    val barcode: String,

    @ColumnInfo(name = "image_uri") // Add this
    val imageUri: String? = null     // Add this line
)