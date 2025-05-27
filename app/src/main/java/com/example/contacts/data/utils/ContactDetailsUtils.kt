package com.example.contacts.data.utils

import com.example.contacts.data.entities.ContactDetails
import com.google.gson.Gson

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