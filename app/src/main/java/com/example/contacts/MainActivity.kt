package com.example.contacts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.contacts.ui.AddContactScreen
import com.example.contacts.ui.ContactsListScreen
import com.example.contacts.ui.theme.ContactsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContactsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContactsApp()
                }
            }
        }
    }
}

@Composable
fun ContactsApp() {
    var currentScreen by remember { mutableStateOf("contacts_list") }
    var selectedContactId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    when (currentScreen) {
        "contacts_list" -> {
            ContactsListScreen(
                context = context,
                onContactClick = { contactId ->
                    selectedContactId = contactId
                    // TODO: 导航到联系人详情页面
                },
                onAddContactClick = {
                    currentScreen = "add_contact"
                },
                onEditContactClick = { contactId ->
                    selectedContactId = contactId
                    // TODO: 导航到编辑联系人页面
                },
                onDeleteContactClick = { contactId ->
                    // 删除操作已在ContactsListScreen中处理
                }
            )
        }
        "add_contact" -> {
            AddContactScreen(
                context = context,
                onNavigateBack = {
                    currentScreen = "contacts_list"
                },
            )
        }
    }
}