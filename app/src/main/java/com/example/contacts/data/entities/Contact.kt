package com.example.contacts.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "source")
    val source: String,
    @ColumnInfo(name = "birth_year")
    val birthYear: Int?,
    @ColumnInfo(name = "birth_month")
    val birthMonth: Int?,
    @ColumnInfo(name = "birth_day")
    val birthDay: Int?,
    @ColumnInfo(name = "real_name")
    val realName: String?,
    @ColumnInfo(name = "phone")
    val phone: String?,
    @ColumnInfo(name = "group_id")
    val groupId: Long? = null, // 分组ID，null表示未分组
    @ColumnInfo(name = "details")
    val details: String? // 用 JSON 存储"细节字段"
)

data class ContactDetails(
    // Extra optional fields
    val nicknames: List<String>?,       // 其他称呼
    val contacts: Map<String, String>?, // 微信/QQ/手机号等
    val hobbies: List<String>?,         // 爱好
    val dislikes: List<String>?,        // 雷点
    val traits: List<String>?,          // 特征
    val addresses: List<String>?,       // 地址

    // Free notes
    val notes: List<String>?,           // 自由备注
    val custom: Map<String, List<String>>? // 自定义字段
) 