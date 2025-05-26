package com.example.contacts

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.google.gson.Gson

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}

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

@Dao
interface ContactDao {
    // Insert a new contact
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long

    // Update an existing contact
    @Update
    suspend fun updateContact(contact: Contact)

    // Delete a contact
    @Delete
    suspend fun deleteContact(contact: Contact)

    // Get all contacts
    @Query("SELECT * FROM contacts")
    suspend fun getAllContacts(): List<Contact>

    // Get a contact by id
    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Long): Contact?

    // Get contacts by name
    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :name || '%'")
    suspend fun getContactsByName(name: String): List<Contact>
}

fun parseDetails(details: String?): ContactDetails? {
    return if (details.isNullOrBlank()) {
        null
    } else {
        try {
            Gson().fromJson(details, ContactDetails::class.java)
        } catch (e: Exception) {
            null
        }
    }
}