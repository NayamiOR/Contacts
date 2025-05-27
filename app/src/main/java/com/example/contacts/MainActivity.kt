package com.example.contacts

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import com.example.contacts.data.ContactDatabase
import com.example.contacts.ui.AddContactScreen
import com.example.contacts.ui.ContactEditScreen
import com.example.contacts.ui.ContactInfoScreen
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
    var previousScreen by remember { mutableStateOf<String?>(null) }
    var backPressedTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current

    // 初始化数据库（带迁移支持）
    val database = remember {
        Room.databaseBuilder(
            context.applicationContext,
            ContactDatabase::class.java,
            "Contacts.db"
        )
        .build()
    }

    // 处理返回键逻辑
    BackHandler {
        when (currentScreen) {
            "contacts_list" -> {
                // 主页面双击返回退出
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime < 2000) {
                    // 2秒内再次按返回键，退出应用
                    (context as? ComponentActivity)?.finish()
                } else {
                    // 第一次按返回键，显示提示
                    backPressedTime = currentTime
                    Toast.makeText(context, "再按一次返回键退出应用", Toast.LENGTH_SHORT).show()
                }
            }

            else -> {
                // 子页面返回上一层
                currentScreen = previousScreen ?: "contacts_list"
                previousScreen = null
                selectedContactId = null
            }
        }
    }

    when (currentScreen) {
        "contacts_list" -> {
            ContactsListScreen(
                context = context,
                database = database,
                onContactClick = { contactId ->
                    selectedContactId = contactId
                    previousScreen = "contacts_list"
                    currentScreen = "contact_info"
                },
                onAddContactClick = {
                    previousScreen = "contacts_list"
                    currentScreen = "add_contact"
                },
                onEditContactClick = { contactId ->
                    selectedContactId = contactId
                    previousScreen = "contacts_list"
                    currentScreen = "edit_contact"
                },
                onDeleteContactClick = { contactId ->
                    // 删除操作已在ContactsListScreen中处理
                    // 这里可以添加额外的删除后处理逻辑
                }
            )
        }

        "add_contact" -> {
            AddContactScreen(
                context = context,
                database = database,
                onNavigateBack = {
                    currentScreen = previousScreen ?: "contacts_list"
                    previousScreen = null
                }
            )
        }

        "contact_info" -> {
            selectedContactId?.let { contactId ->
                ContactInfoScreen(
                    contactId = contactId,
                    context = context,
                    database = database,
                    onNavigateBack = {
                        currentScreen = previousScreen ?: "contacts_list"
                        previousScreen = null
                    },
                    onEditContact = { editContactId ->
                        selectedContactId = editContactId
                        previousScreen = "contact_info"
                        currentScreen = "edit_contact"
                    },
                    onDeleteContact = {
                        // 删除后返回列表页面
                        currentScreen = "contacts_list"
                        previousScreen = null
                        selectedContactId = null
                    }
                )
            } ?: run {
                // 如果没有选中的联系人ID，返回列表页面
                currentScreen = "contacts_list"
                previousScreen = null
            }
        }

        "edit_contact" -> {
            selectedContactId?.let { contactId ->
                ContactEditScreen(
                    contactId = contactId,
                    context = context,
                    database = database,
                    onNavigateBack = {
                        // 编辑页面返回到之前的页面（通常是详情页面或列表页面）
                        currentScreen = previousScreen ?: "contacts_list"
                        previousScreen = null
                    }
                )
            } ?: run {
                // 如果没有选中的联系人ID，返回列表页面
                currentScreen = "contacts_list"
                previousScreen = null
            }
        }
    }
}