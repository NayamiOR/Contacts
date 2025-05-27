package com.example.contacts.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.contacts.data.entities.Contact
import com.example.contacts.data.entities.ContactWithGroupInfo

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