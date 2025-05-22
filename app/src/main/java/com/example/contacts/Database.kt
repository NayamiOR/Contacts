package com.example.contacts

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.sql.Date

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class ContactDatabase {
    abstract fun contactDao(): ContactDao
}

@Entity
data class Contact(
    @ColumnInfo(name = "name")
    @PrimaryKey val name: String,
    @ColumnInfo(name = "source")
    @PrimaryKey val source: String,
    @ColumnInfo(name = "birth_year")
    val birthYear: Int?,
    @ColumnInfo(name = "birth_month")
    val birthMonth: Int?,
    @ColumnInfo(name = "birth_day")
    val birthDay: Int?,
    @ColumnInfo(name = "birth_date")
    val realName: String?,
    @ColumnInfo(name = "phone")
    val phone: String?,
    @ColumnInfo(name = "details")
    val details: String? // 用 JSON 存储“细节字段”
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

@Dao
interface ContactDao {
    // Insert a new contact
    fun insertContact(contact: Contact)

    // Update an existing contact
    fun updateContact(contact: Contact)

    // Delete a contact
    fun deleteContact(contact: Contact)

    // Get all contacts
    fun getAllContacts(): List<Contact>

    // Get a contact by name
    fun getContactByName(name: String): Contact?
}

fun ParseDetails(details: String): ContactDetails {
    TODO()
}