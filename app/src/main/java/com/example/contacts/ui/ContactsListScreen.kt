package com.example.contacts.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.contacts.data.entities.Contact
import com.example.contacts.data.ContactDatabase
import com.example.contacts.data.entities.ContactGroup
import com.example.contacts.PinyinUtils
import com.example.contacts.R
import com.example.contacts.ui.components.AlphabetSortedContactsPage
import com.example.contacts.ui.components.GroupedContactsPage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsListScreen(
    onContactClick: (String) -> Unit = {},
    onAddContactClick: () -> Unit = {},
    onEditContactClick: (String) -> Unit = {},
    onDeleteContactClick: (String) -> Unit = {},
    context: Context = LocalContext.current,
    database: ContactDatabase? = null
) {
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedContacts by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // 分页状态
    var currentPage by remember { mutableStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { 2 })

    // 分组相关状态
    var groups by remember { mutableStateOf<List<ContactGroup>>(emptyList()) }
    var groupedContactsByGroup by remember {
        mutableStateOf<Map<ContactGroup?, List<Contact>>>(
            emptyMap()
        )
    }

    // 删除确认对话框状态
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var contactToDelete by remember { mutableStateOf<Contact?>(null) }

    // 字母索引相关状态
    val listState = rememberLazyListState()

    val scope = rememberCoroutineScope()

    // 分组折叠状态
    var collapsedGroups by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // 全部折叠/展开功能
    fun toggleAllGroups() {
        val allGroupIds = groups.map { it.id }.toSet() + setOf(-1L) // 包含未分组
        collapsedGroups = if (collapsedGroups.size == allGroupIds.size) {
            emptySet() // 如果全部折叠，则全部展开
        } else {
            allGroupIds // 否则全部折叠
        }
    }

    // 初始化数据库
    val contactDatabase = database ?: remember {
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
                val loadedContacts = contactDatabase.contactDao().getAllContacts()
                // 应用拼音排序
                contacts = loadedContacts.sortedWith { contact1, contact2 ->
                    PinyinUtils.compareStrings(contact1.name, contact2.name)
                }

                // 加载分组数据
                val loadedGroups = contactDatabase.groupDao().getAllGroups()
                groups = loadedGroups

                // 按分组分类联系人
                val ungroupedContacts = loadedContacts.filter { it.groupId == null }
                val groupedMap = mutableMapOf<ContactGroup?, List<Contact>>()

                // 添加未分组联系人
                if (ungroupedContacts.isNotEmpty()) {
                    groupedMap[null] = ungroupedContacts.sortedWith { contact1, contact2 ->
                        PinyinUtils.compareStrings(contact1.name, contact2.name)
                    }
                }

                // 添加各分组的联系人
                loadedGroups.forEach { group ->
                    val groupContacts = loadedContacts.filter { it.groupId == group.id }
                    if (groupContacts.isNotEmpty()) {
                        groupedMap[group] = groupContacts.sortedWith { contact1, contact2 ->
                            PinyinUtils.compareStrings(contact1.name, contact2.name)
                        }
                    }
                }

                groupedContactsByGroup = groupedMap
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
                    contactDatabase.contactDao().deleteContact(contact)
                }
            }
            val loadedContacts = contactDatabase.contactDao().getAllContacts()
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
                contactDatabase.contactDao().deleteContact(contact)
                val loadedContacts = contactDatabase.contactDao().getAllContacts()
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

            //val shareIntent = Intent().apply {
            //    Intent.setAction = Intent.ACTION_SEND
            //    Intent.setType = "text/plain"
            //    putExtra(Intent.EXTRA_TEXT, shareText)
            //    putExtra(Intent.EXTRA_SUBJECT, "联系人信息")
            //}

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "联系人信息")
            }

            context.startActivity(Intent.createChooser(shareIntent, "分享联系人"))
            exitSelectionMode() // 分享后退出选择模式
        }
    }

    // 切换分组折叠状态
    fun toggleGroupCollapse(groupId: Long?) {
        if (groupId == null) {
            // 处理未分组的折叠状态，使用特殊ID -1
            val ungroupedId = -1L
            collapsedGroups = if (collapsedGroups.contains(ungroupedId)) {
                collapsedGroups - ungroupedId
            } else {
                collapsedGroups + ungroupedId
            }
        } else {
            collapsedGroups = if (collapsedGroups.contains(groupId)) {
                collapsedGroups - groupId
            } else {
                collapsedGroups + groupId
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isSelectionMode) "已选择 ${selectedContacts.size} 项" else "联系人",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Companion.Bold
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
                                        R.drawable.baseline_check_box_24
                                    else
                                        R.drawable.baseline_check_box_outline_blank_24
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
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.Companion.fillMaxSize()
            ) {
                // 搜索栏
                Card(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(10.dp),
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
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(10.dp),
                        placeholder = { Text("搜索") },
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

                // 分页标签
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.Companion.fillMaxWidth()
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        text = { Text("按字母") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        text = { Text("按分组") }
                    )
                }

                // 分页内容
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.Companion.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> {
                            // 第一页：按字母排序
                            AlphabetSortedContactsPage(
                                filteredContacts = filteredContacts,
                                groupedContacts = groupedContacts,
                                alphabetIndex = alphabetIndex,
                                letterToIndex = letterToIndex,
                                listState = listState,
                                searchQuery = searchQuery,
                                isSelectionMode = isSelectionMode,
                                selectedContacts = selectedContacts,
                                onContactClick = { contact ->
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
                                onContactLongClick = { contact ->
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedContacts = setOf(contact.id)
                                    }
                                },
                                onEditContactClick = onEditContactClick,
                                onDeleteContactClick = { contact ->
                                    showDeleteConfirmation(contact)
                                },
                                scope = scope
                            )
                        }

                        1 -> {
                            Column {
                                GroupedContactsPage(
                                    groupedContactsByGroup = groupedContactsByGroup,
                                    searchQuery = searchQuery,
                                    isSelectionMode = isSelectionMode,
                                    selectedContacts = selectedContacts,
                                    collapsedGroups = collapsedGroups,
                                    onToggleGroupCollapse = ::toggleGroupCollapse,
                                    onContactClick = { contact ->
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
                                    onContactLongClick = { contact ->
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedContacts = setOf(contact.id)
                                        }
                                    },
                                    onEditContactClick = onEditContactClick,
                                    onDeleteContactClick = { contact ->
                                        showDeleteConfirmation(contact)
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
                    modifier = Modifier.Companion
                        .align(Alignment.Companion.BottomEnd)
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.Companion.padding(8.dp),
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