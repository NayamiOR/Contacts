package com.example.contacts.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.contacts.Contact
import com.example.contacts.ContactDatabase
import com.example.contacts.PinyinUtils
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsListScreen(
    onContactClick: (String) -> Unit = {},
    onAddContactClick: () -> Unit = {},
    onEditContactClick: (String) -> Unit = {},
    onDeleteContactClick: (String) -> Unit = {},
    context: Context = LocalContext.current
) {
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedContacts by remember { mutableStateOf<Set<Long>>(emptySet()) }
    
    // 删除确认对话框状态
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var contactToDelete by remember { mutableStateOf<Contact?>(null) }

    // 字母索引相关状态
    val listState = rememberLazyListState()
    
    val scope = rememberCoroutineScope()

    // 初始化数据库
    val database = remember {
        Room.databaseBuilder(
            context.applicationContext,
            ContactDatabase::class.java,
            "Contacts.db"
        ).build()
    }

    // 加载联系人数据
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val loadedContacts = database.contactDao().getAllContacts()
                // 应用拼音排序
                contacts = loadedContacts.sortedWith { contact1, contact2 ->
                    PinyinUtils.compareStrings(contact1.name, contact2.name)
                }
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    // 搜索过滤和排序
    val filteredContacts = remember(contacts, searchQuery) {
        val filtered = if (searchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true) ||
                        contact.source.contains(searchQuery, ignoreCase = true) ||
                        contact.realName?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        // 使用拼音排序
        filtered.sortedWith { contact1, contact2 ->
            PinyinUtils.compareStrings(contact1.name, contact2.name)
        }
    }

    // 按首字母分组（使用拼音首字母）
    val groupedContacts = remember(filteredContacts) {
        filteredContacts.groupBy { contact ->
            PinyinUtils.getFirstLetter(contact.name)
        }.toSortedMap()
    }

    // 获取所有字母索引
    val alphabetIndex = remember(groupedContacts) {
        val letters = mutableListOf<Char>()
        // 如果有非字母开头的联系人，先添加#到最上方
        if (groupedContacts.containsKey('#')) {
            letters.add('#')
        }
        // 添加A-Z
        for (c in 'A'..'Z') {
            letters.add(c)
        }
        letters
    }

    // 计算每个字母对应的列表项索引
    val letterToIndex = remember(groupedContacts) {
        val map = mutableMapOf<Char, Int>()
        var currentIndex = 0
        groupedContacts.forEach { (letter, contacts) ->
            map[letter] = currentIndex
            currentIndex += contacts.size + 1 // +1 for header
        }
        map
    }

    // 退出选择模式
    fun exitSelectionMode(showToast: Boolean = false) {
        isSelectionMode = false
        selectedContacts = emptySet()
        if (showToast) {
            Toast.makeText(context, "已退出选择模式", Toast.LENGTH_SHORT).show()
        }
    }

    // 全选/取消全选
    fun toggleSelectAll() {
        selectedContacts = if (selectedContacts.size == filteredContacts.size) {
            emptySet()
        } else {
            filteredContacts.map { it.id }.toSet()
        }
    }

    // 显示批量删除确认对话框
    fun showBatchDeleteConfirmation() {
        showBatchDeleteDialog = true
    }

    // 执行批量删除
    fun executeBatchDelete() {
        scope.launch {
            selectedContacts.forEach { contactId ->
                contacts.find { it.id == contactId }?.let { contact ->
                    database.contactDao().deleteContact(contact)
                }
            }
            val loadedContacts = database.contactDao().getAllContacts()
            // 应用拼音排序
            contacts = loadedContacts.sortedWith { contact1, contact2 ->
                PinyinUtils.compareStrings(contact1.name, contact2.name)
            }
            exitSelectionMode()
            Toast.makeText(context, "已删除 ${selectedContacts.size} 个联系人", Toast.LENGTH_SHORT)
                .show()
        }
        showBatchDeleteDialog = false
    }

    // 显示单个删除确认对话框
    fun showDeleteConfirmation(contact: Contact) {
        contactToDelete = contact
        showDeleteDialog = true
    }

    // 执行单个删除
    fun executeSingleDelete() {
        contactToDelete?.let { contact ->
            scope.launch {
                database.contactDao().deleteContact(contact)
                val loadedContacts = database.contactDao().getAllContacts()
                // 应用拼音排序
                contacts = loadedContacts.sortedWith { contact1, contact2 ->
                    PinyinUtils.compareStrings(contact1.name, contact2.name)
                }
                Toast.makeText(context, "已删除联系人 ${contact.name}", Toast.LENGTH_SHORT).show()
                onDeleteContactClick(contact.id.toString())
            }
        }
        showDeleteDialog = false
        contactToDelete = null
    }

    // 批量分享
    fun shareSelectedContacts() {
        val selectedContactsList = contacts.filter { selectedContacts.contains(it.id) }
        if (selectedContactsList.isNotEmpty()) {
            val shareText = buildString {
                appendLine("分享的联系人信息:")
                appendLine()
                selectedContactsList.forEach { contact ->
                    appendLine("姓名: ${contact.name}")
                    appendLine("来源: ${contact.source}")
                    contact.phone?.takeIf { it.isNotBlank() }?.let {
                        appendLine("手机号: $it")
                    }
                    appendLine("---")
                }
            }

            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "联系人信息")
            }

            context.startActivity(android.content.Intent.createChooser(shareIntent, "分享联系人"))
            exitSelectionMode() // 分享后退出选择模式
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isSelectionMode) "已选择 ${selectedContacts.size} 项" else "联系人",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { toggleSelectAll() }) {
                            Icon(
                                painter = painterResource(
                                    id = if (selectedContacts.size == filteredContacts.size)
                                        com.example.contacts.R.drawable.baseline_check_box_24
                                    else
                                        com.example.contacts.R.drawable.baseline_check_box_outline_blank_24
                                ),
                                contentDescription = if (selectedContacts.size == filteredContacts.size)
                                    "取消全选"
                                else
                                    "全选",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { exitSelectionMode(showToast = false) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "取消选择",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        // 添加联系人按钮
                        IconButton(onClick = onAddContactClick) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "添加联系人",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        // 处理返回键：在选择模式下先退出选择模式，否则正常返回
        BackHandler(enabled = isSelectionMode) {
            exitSelectionMode(showToast = false)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 搜索栏
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            // 搜索时如果在选择模式，退出选择模式
                            if (isSelectionMode) {
                                exitSelectionMode()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("搜索联系人...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "搜索"
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // 联系人列表
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    filteredContacts.isEmpty() -> {
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
                                    contentDescription = "无联系人",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (searchQuery.isBlank()) "还没有联系人" else "未找到匹配的联系人",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (searchQuery.isBlank()) {
                                    Button(onClick = onAddContactClick) {
                                        Text("添加第一个联系人")
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                                state = listState,
                            modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 40.dp, // 为右侧字母索引条留出更多空间
                                    top = 8.dp,
                                    bottom = 8.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                groupedContacts.forEach { (letter, contactsInGroup) ->
                                    // 字母分组标题
                                    item(key = "header_$letter") {
                                        SectionHeader(letter = letter)
                                    }

                                    // 该字母下的联系人
                                    items(
                                        items = contactsInGroup,
                                        key = { contact -> contact.id }
                                    ) { contact ->
                                ContactItem(
                                    contact = contact,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedContacts.contains(contact.id),
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedContacts =
                                                if (selectedContacts.contains(contact.id)) {
                                                    selectedContacts - contact.id
                                                } else {
                                                    selectedContacts + contact.id
                                                }
                                        } else {
                                            onContactClick(contact.id.toString())
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedContacts = setOf(contact.id)
                                        }
                                    },
                                    onEditClick = { onEditContactClick(contact.id.toString()) },
                                    onDeleteClick = {
                                        showDeleteConfirmation(contact)
                                            }
                                        )
                                    }
                                }
                            }

                            // 字母索引条
                            if (searchQuery.isBlank() && groupedContacts.isNotEmpty()) {
                                AlphabetIndexBar(
                                    modifier = Modifier.align(Alignment.CenterEnd),
                                    alphabetIndex = alphabetIndex,
                                    onLetterSelected = { letter ->
                                        letterToIndex[letter]?.let { index ->
                                            scope.launch {
                                                // 使用scrollToItem获得更快的响应
                                                listState.scrollToItem(index)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 批量操作按钮
            if (isSelectionMode && selectedContacts.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 分享按钮
                        IconButton(
                            onClick = { shareSelectedContacts() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "分享",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // 删除按钮
                        IconButton(
                            onClick = { showBatchDeleteConfirmation() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // 删除确认对话框
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("删除联系人") },
                text = { Text("确定要删除联系人 \"${contactToDelete?.name}\" 吗？此操作无法撤销。") },
                confirmButton = {
                    TextButton(
                        onClick = { executeSingleDelete() }
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

        // 批量删除确认对话框
        if (showBatchDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showBatchDeleteDialog = false },
                title = { Text("批量删除联系人") },
                text = { Text("确定要删除选中的 ${selectedContacts.size} 个联系人吗？此操作无法撤销。") },
                confirmButton = {
                    TextButton(
                        onClick = { executeBatchDelete() }
                    ) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBatchDeleteDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContactItem(
    contact: Contact,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选择框
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            // 头像
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 联系人信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = contact.source,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 显示手机号（如果有）
                contact.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// 分组标题组件
@Composable
fun SectionHeader(letter: Char) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = letter.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// 字母索引条组件
@Composable
fun AlphabetIndexBar(
    modifier: Modifier = Modifier,
    alphabetIndex: List<Char>,
    onLetterSelected: (Char) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var currentSelectedLetter by remember { mutableStateOf<Char?>(null) }
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = modifier.width(80.dp) // 增加整体宽度以容纳气泡
    ) {
        // 字母栏固定在右侧
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(32.dp)
                .align(Alignment.CenterEnd) // 固定在右侧
                .padding(end = 8.dp)
                .pointerInput(alphabetIndex) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            // 计算起始位置对应的字母
                            val y = offset.y
                            val totalHeight = size.height.toFloat()
                            val letterIndex = ((y / totalHeight) * alphabetIndex.size).roundToInt()
                                .coerceIn(0, alphabetIndex.size - 1)
                            val selectedLetter = alphabetIndex[letterIndex]
                            currentSelectedLetter = selectedLetter
                            // 添加触觉反馈
                            hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onLetterSelected(selectedLetter)
                        },
                        onDragEnd = {
                            isDragging = false
                            currentSelectedLetter = null
                        }
                    ) { change, _ ->
                        // 拖动过程中持续更新选中的字母
                        val y = change.position.y.coerceIn(0f, size.height.toFloat())
                        val totalHeight = size.height.toFloat()
                        val letterIndex = ((y / totalHeight) * alphabetIndex.size).roundToInt()
                            .coerceIn(0, alphabetIndex.size - 1)
                        val selectedLetter = alphabetIndex[letterIndex]

                        // 只有当选中的字母发生变化时才触发回调
                        if (selectedLetter != currentSelectedLetter) {
                            currentSelectedLetter = selectedLetter
                            // 添加触觉反馈
                            hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onLetterSelected(selectedLetter)
                        }
                    }
                }
                .pointerInput(alphabetIndex) {
                    // 处理点击事件
                    detectTapGestures { offset ->
                        val y = offset.y
                        val totalHeight = size.height.toFloat()
                        val letterIndex = ((y / totalHeight) * alphabetIndex.size).roundToInt()
                            .coerceIn(0, alphabetIndex.size - 1)
                        onLetterSelected(alphabetIndex[letterIndex])
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            alphabetIndex.forEach { letter ->
                val isSelected = isDragging && currentSelectedLetter == letter

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = if (isSelected) 12.sp else 10.sp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 浮动字母提示 - 显示在左侧
        AnimatedVisibility(
            visible = isDragging && currentSelectedLetter != null,
            enter = fadeIn(animationSpec = tween(100)) + scaleIn(
                animationSpec = tween(100),
                initialScale = 0.8f
            ),
            exit = fadeOut(animationSpec = tween(100)) + scaleOut(
                animationSpec = tween(100),
                targetScale = 0.8f
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp) // 与左边缘的距离
        ) {
            Card(
                modifier = Modifier.size(40.dp), // 进一步缩小气泡尺寸
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(30)// 使用圆形气泡
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentSelectedLetter?.toString() ?: "",
                        style = MaterialTheme.typography.titleMedium, // 调整字体大小
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewContactsListScreen() {
    ContactsListScreen(
        onContactClick = {},
        onAddContactClick = {},
        onEditContactClick = {},
        onDeleteContactClick = {}
    )
}