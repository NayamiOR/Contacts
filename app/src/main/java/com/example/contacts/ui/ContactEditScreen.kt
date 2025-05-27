package com.example.contacts.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.contacts.data.ContactDatabase
import com.example.contacts.data.entities.Contact
import com.example.contacts.data.entities.ContactGroup
import com.example.contacts.ui.components.CollapsibleState
import com.example.contacts.ui.components.ContactFormContent
import com.example.contacts.ui.components.ContactFormState
import com.example.contacts.ui.components.createContactDetailsFromFormState
import com.example.contacts.ui.components.initializeFormStateFromContact
import com.google.gson.Gson
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditScreen(
    contactId: String,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    database: ContactDatabase? = null,
    onNavigateBack: () -> Unit = {},
) {
    // 状态管理
    var contact by remember { mutableStateOf<Contact?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var formState by remember { mutableStateOf(ContactFormState()) }
    val collapsibleState = remember { CollapsibleState() }
    
    // 分组相关状态
    var groups by remember { mutableStateOf<List<ContactGroup>>(emptyList()) }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }

    // 协程作用域和数据库
    val scope = rememberCoroutineScope()
    val databaseInstance = database ?: remember {
        Room.databaseBuilder(
            context.applicationContext,
            ContactDatabase::class.java,
            "Contacts.db"
        ).build()
    }

    // 加载联系人数据和分组数据
    LaunchedEffect(contactId) {
        scope.launch {
            try {
                // 加载联系人信息
                val loadedContact = databaseInstance.contactDao().getContactById(contactId.toLong())
                contact = loadedContact

                // 加载分组数据
                val loadedGroups = databaseInstance.groupDao().getAllGroups()
                groups = loadedGroups

                // 初始化表单状态
                loadedContact?.let {
                    formState = initializeFormStateFromContact(it)
                    selectedGroupId = it.groupId
                }

                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "加载联系人信息失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = modifier,
                title = {
                    Text(
                        text = if (contact != null) "编辑 ${contact!!.name}" else "编辑联系人",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (formState.name.isBlank() || formState.source.isBlank()) {
                                Toast.makeText(context, "姓名和来源不能为空", Toast.LENGTH_SHORT)
                                    .show()
                                return@IconButton
                            }

                            scope.launch {
                                try {
                                    val details = createContactDetailsFromFormState(formState)
                                    val detailsJson = Gson().toJson(details)

                                    // 更新联系人
                                    contact?.let { originalContact ->
                                        val updatedContact = originalContact.copy(
                                            name = formState.name,
                                            source = formState.source,
                                            birthYear = formState.birthYear.toIntOrNull(),
                                            birthMonth = formState.birthMonth.toIntOrNull(),
                                            birthDay = formState.birthDay.toIntOrNull(),
                                            realName = formState.realName.takeIf { it.isNotBlank() },
                                            phone = formState.contacts["手机号"]?.takeIf { it.isNotBlank() },
                                            groupId = selectedGroupId, // 更新分组ID
                                            details = detailsJson
                                        )

                                        databaseInstance.contactDao().updateContact(updatedContact)

                                        Log.d("ContactEditScreen", "联系人已更新: $updatedContact")
                                        onNavigateBack()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        context,
                                        "更新联系人失败: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "保存",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            contact == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "未找到联系人信息",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("返回")
                        }
                    }
                }
            }

            else -> {
                ContactFormContent(
                    formState = formState,
                    collapsibleState = collapsibleState,
                    groups = groups,
                    selectedGroupId = selectedGroupId,
                    onGroupSelected = { groupId ->
                        selectedGroupId = groupId
                    },
                    onGroupAdded = { newGroup ->
                        // 添加新分组到数据库
                        scope.launch {
                            try {
                                val groupId = databaseInstance.groupDao().insertGroup(newGroup)
                                val insertedGroup = newGroup.copy(id = groupId)
                                groups = groups + insertedGroup
                                selectedGroupId = groupId
                                Toast.makeText(context, "分组 \"${newGroup.name}\" 已创建", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "创建分组失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactEditScreenPreview() {
    ContactEditScreen(
        contactId = "1",
        modifier = Modifier,
        context = LocalContext.current,
    )
}