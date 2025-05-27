package com.example.contacts.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_groups")
data class ContactGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "color")
    val color: String = "#2196F3", // 默认蓝色
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) 