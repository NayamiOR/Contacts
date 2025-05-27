package com.example.contacts.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.contacts.Contact
import com.example.contacts.ContactDatabase
import com.example.contacts.parseDetails
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactInfoScreen(
    contactId: String,
    onNavigateBack: () -> Unit = {},
    onEditContact: (String) -> Unit = {},
    onDeleteContact: () -> Unit = {},
    context: Context = LocalContext.current,
    database: ContactDatabase? = null
) {
    var contact by remember { mutableStateOf<Contact?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isFavorite by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 初始化数据库
    val databaseInstance = database ?: remember {
        Room.databaseBuilder(
            context.applicationContext,
            ContactDatabase::class.java,
            "Contacts.db"
        ).build()
    }

    // 加载联系人数据
    LaunchedEffect(contactId) {
        scope.launch {
            try {
                contact = databaseInstance.contactDao().getContactById(contactId.toLong())
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "加载联系人信息失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除联系人") },
            text = { Text("确定要删除这个联系人吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            contact?.let {
                                databaseInstance.contactDao().deleteContact(it)
                                Toast.makeText(context, "联系人已删除", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = contact?.name ?: "联系人详情",
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
                            contact?.let { onEditContact(it.id.toString()) }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            // 底部悬浮操作栏
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 分享按钮
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                contact?.let { shareContact(context, it) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "分享",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "分享",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 收藏按钮
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                isFavorite = !isFavorite
                                Toast.makeText(
                                    context,
                                    if (isFavorite) "已添加到收藏" else "已取消收藏",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "取消收藏" else "收藏",
                                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "收藏",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 删除按钮
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = "删除",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "未找到联系人",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "未找到联系人信息",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onNavigateBack) {
                            Text("返回")
                        }
                    }
                }
            }

            else -> {
                ContactInfoContent(
                    contact = contact!!,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun ContactInfoContent(
    contact: Contact,
    modifier: Modifier = Modifier
) {
    val contactDetails = parseDetails(contact.details)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 头像和基本信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 头像
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.take(1),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 姓名
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // 来源
                Text(
                    text = "来源: ${contact.source}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // 真实姓名（如果有）
                contact.realName?.takeIf { it.isNotBlank() }?.let { realName ->
                    Text(
                        text = "真实姓名: $realName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 基本信息卡片
        if (contact.birthYear != null || contact.birthMonth != null || contact.birthDay != null || contact.phone != null) {
            InfoCard(title = "基本信息") {
                // 生日信息
                if (contact.birthYear != null || contact.birthMonth != null || contact.birthDay != null) {
                    val birthday = buildString {
                        contact.birthYear?.let { append("${it}年") }
                        contact.birthMonth?.let { append("${it}月") }
                        contact.birthDay?.let { append("${it}日") }
                    }
                    if (birthday.isNotBlank()) {
                        InfoItem(label = "生日", value = birthday)
                    }
                }

                // 手机号
                contact.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    InfoItem(label = "手机号", value = phone)
                }
            }
        }

        // 联系方式卡片
        contactDetails?.contacts?.takeIf { it.isNotEmpty() }?.let { contacts ->
            InfoCard(title = "联系方式") {
                contacts.forEach { (type, value) ->
                    if (value.isNotBlank()) {
                        InfoItem(label = type, value = value)
                    }
                }
            }
        }

        // 昵称卡片
        contactDetails?.nicknames?.takeIf { it.isNotEmpty() }?.let { nicknames ->
            InfoCard(title = "称呼") {
                ChipGroup(items = nicknames)
            }
        }

        // 爱好卡片
        contactDetails?.hobbies?.takeIf { it.isNotEmpty() }?.let { hobbies ->
            InfoCard(title = "爱好") {
                ChipGroup(items = hobbies)
            }
        }

        // 雷点卡片
        contactDetails?.dislikes?.takeIf { it.isNotEmpty() }?.let { dislikes ->
            InfoCard(title = "雷点") {
                ChipGroup(items = dislikes, isNegative = true)
            }
        }

        // 特点卡片
        contactDetails?.traits?.takeIf { it.isNotEmpty() }?.let { traits ->
            InfoCard(title = "特点") {
                ChipGroup(items = traits)
            }
        }

        // 地址卡片
        contactDetails?.addresses?.takeIf { it.isNotEmpty() }?.let { addresses ->
            InfoCard(title = "地址") {
                addresses.forEach { address ->
                    if (address.isNotBlank()) {
                        InfoItem(label = "地址", value = address)
                    }
                }
            }
        }

        // 备注卡片
        contactDetails?.notes?.takeIf { it.isNotEmpty() }?.let { notes ->
            InfoCard(title = "备注") {
                notes.forEach { note ->
                    if (note.isNotBlank()) {
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 自定义字段卡片
        contactDetails?.custom?.takeIf { it.isNotEmpty() }?.let { customFields ->
            customFields.forEach { (fieldName, values) ->
                if (values.isNotEmpty()) {
                    InfoCard(title = fieldName) {
                        ChipGroup(items = values)
                    }
                }
            }
        }

        // 底部间距，避免被悬浮栏遮挡
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoChip(
    text: String,
    isNegative: Boolean = false
) {
    AssistChip(
        onClick = { },
        label = { Text(text) },
        modifier = Modifier.padding(end = 8.dp, bottom = 4.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isNegative)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.secondaryContainer,
            labelColor = if (isNegative)
                MaterialTheme.colorScheme.onErrorContainer
            else
                MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipGroup(
    items: List<String>,
    isNegative: Boolean = false
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            if (item.isNotBlank()) {
                InfoChip(text = item, isNegative = isNegative)
            }
        }
    }
}

// 分享联系人功能
fun shareContact(context: Context, contact: Contact) {
    val shareText = buildString {
        appendLine("联系人信息:")
        appendLine("姓名: ${contact.name}")
        appendLine("来源: ${contact.source}")

        contact.realName?.takeIf { it.isNotBlank() }?.let {
            appendLine("真实姓名: $it")
        }

        if (contact.birthYear != null || contact.birthMonth != null || contact.birthDay != null) {
            val birthday = buildString {
                contact.birthYear?.let { append("${it}年") }
                contact.birthMonth?.let { append("${it}月") }
                contact.birthDay?.let { append("${it}日") }
            }
            if (birthday.isNotBlank()) {
                appendLine("生日: $birthday")
            }
        }

        contact.phone?.takeIf { it.isNotBlank() }?.let {
            appendLine("手机号: $it")
        }

        val details = parseDetails(contact.details)
        details?.contacts?.forEach { (type, value) ->
            if (value.isNotBlank()) {
                appendLine("$type: $value")
            }
        }
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "联系人: ${contact.name}")
    }

    context.startActivity(Intent.createChooser(shareIntent, "分享联系人"))
}

@Preview(showBackground = true)
@Composable
fun ContactInfoScreenPreview() {
    ContactInfoScreen(
        contactId = "1",
        onNavigateBack = {},
        onEditContact = {},
        onDeleteContact = {}
    )
}