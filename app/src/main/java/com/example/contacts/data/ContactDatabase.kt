package com.example.contacts.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.contacts.data.dao.ContactDao
import com.example.contacts.data.dao.GroupDao
import com.example.contacts.data.entities.Contact
import com.example.contacts.data.entities.ContactGroup

@Database(entities = [Contact::class, ContactGroup::class], version = 2, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun groupDao(): GroupDao
} 