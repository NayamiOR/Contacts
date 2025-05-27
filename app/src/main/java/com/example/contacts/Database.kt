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

@Database(entities = [Contact::class, ContactGroup::class], version = 2, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun groupDao(): GroupDao
}

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

// 联系人与分组的关联数据类
data class ContactWithGroup(
    val contact: Contact,
    val group: ContactGroup?
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

    // Get contacts by group
    @Query("SELECT * FROM contacts WHERE group_id = :groupId")
    suspend fun getContactsByGroup(groupId: Long): List<Contact>

    // Get ungrouped contacts
    @Query("SELECT * FROM contacts WHERE group_id IS NULL")
    suspend fun getUngroupedContacts(): List<Contact>

    // Get contacts with group info
    @Query("""
        SELECT c.*, g.name as group_name, g.color as group_color 
        FROM contacts c 
        LEFT JOIN contact_groups g ON c.group_id = g.id
    """)
    suspend fun getContactsWithGroup(): List<ContactWithGroupInfo>

    // Update contact group
    @Query("UPDATE contacts SET group_id = :groupId WHERE id = :contactId")
    suspend fun updateContactGroup(contactId: Long, groupId: Long?)

    // Clear all contacts (for testing)
    @Query("DELETE FROM contacts")
    suspend fun clearAllContacts()

    // Get contacts count
    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getContactsCount(): Int

    // Get contacts count by group
    @Query("SELECT COUNT(*) FROM contacts WHERE group_id = :groupId")
    suspend fun getContactsCountByGroup(groupId: Long): Int

    // Get ungrouped contacts count
    @Query("SELECT COUNT(*) FROM contacts WHERE group_id IS NULL")
    suspend fun getUngroupedContactsCount(): Int
}

@Dao
interface GroupDao {
    // Insert a new group
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ContactGroup): Long

    // Update a group
    @Update
    suspend fun updateGroup(group: ContactGroup)

    // Delete a group
    @Delete
    suspend fun deleteGroup(group: ContactGroup)

    // Get all groups
    @Query("SELECT * FROM contact_groups ORDER BY name")
    suspend fun getAllGroups(): List<ContactGroup>

    // Get group by id
    @Query("SELECT * FROM contact_groups WHERE id = :id")
    suspend fun getGroupById(id: Long): ContactGroup?

    // Get group by name
    @Query("SELECT * FROM contact_groups WHERE name = :name")
    suspend fun getGroupByName(name: String): ContactGroup?

    // Delete group and move contacts to ungrouped
    @Query("UPDATE contacts SET group_id = NULL WHERE group_id = :groupId")
    suspend fun moveContactsToUngrouped(groupId: Long)
}

// 用于查询结果的数据类
data class ContactWithGroupInfo(
    val id: Long,
    val name: String,
    val source: String,
    val birthYear: Int?,
    val birthMonth: Int?,
    val birthDay: Int?,
    val realName: String?,
    val phone: String?,
    val groupId: Long?,
    val details: String?,
    val groupName: String?,
    val groupColor: String?
)

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