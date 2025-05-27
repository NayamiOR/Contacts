package com.example.contacts.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.contacts.data.entities.ContactGroup

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