package com.example.contacts.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.contacts.ui.components.CollapsibleContactCard
import com.example.contacts.ui.components.CollapsibleDetailCard

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSaveContact: (String, String, Int?, Int?, Int?, String?, String?, String?) -> Unit = { _, _, _, _, _, _, _, _ -> }
) {
    // 基本状态变量
    var name by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var realName by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<String?>(null) }

    // 详细信息状态变量
    var nicknames by remember { mutableStateOf(mutableStateListOf<String>()) }
    var contacts by remember { mutableStateOf(mutableStateMapOf<String, String>(
        "微信" to "",
        "QQ" to "",
        "手机号" to ""
    )) }
    var hobbies by remember { mutableStateOf(mutableStateListOf<String>()) }
    var dislikes by remember { mutableStateOf(mutableStateListOf<String>()) }
    var traits by remember { mutableStateOf(mutableStateListOf<String>()) }
    var addresses by remember { mutableStateOf(mutableStateListOf<String>()) }
    var notes by remember { mutableStateOf(mutableStateListOf<String>()) }
    var customFields by remember { mutableStateOf(mutableStateMapOf<String, MutableList<String>>()) }

    // 折叠状态
    var isBasicInfoExpanded by remember { mutableStateOf(true) }
    var isBirthdayExpanded by remember { mutableStateOf(false) }
    var isNicknamesExpanded by remember { mutableStateOf(false) }
    var isContactsExpanded by remember { mutableStateOf(false) }
    var isHobbiesExpanded by remember { mutableStateOf(false) }
    var isDislikesExpanded by remember { mutableStateOf(false) }
    var isTraitsExpanded by remember { mutableStateOf(false) }
    var isAddressesExpanded by remember { mutableStateOf(false) }
    var isNotesExpanded by remember { mutableStateOf(false) }
    var isCustomFieldsExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = modifier,
                title = { Text(text = "添加联系人") },
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
                            if (name.isNotBlank() && source.isNotBlank()) {
                                // TODO: 构建包含详细信息的JSON字符串
                                //onSaveContact(
                                //    name,
                                //    source,
                                //    birthYear.toIntOrNull(),
                                //    birthMonth.toIntOrNull(),
                                //    birthDay.toIntOrNull(),
                                //    realName.ifBlank { null },
                                //    phone.ifBlank { null },
                                //    null // 这里应该传入构建好的详细信息JSON
                                //)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 头像区域卡片
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
                    // 头像显示区域
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                if (avatarUri == null) MaterialTheme.colorScheme.surfaceVariant
                                else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable {
                                // TODO: 打开图片选择器
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUri == null) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "默认头像",
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // TODO: 显示选择的头像图片
                            // AsyncImage 或其他图片加载组件
                        }
                    }
                    
                    // 更换头像按钮
                    OutlinedButton(
                        onClick = {
                            // TODO: 打开图片选择器
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "更换头像",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (avatarUri == null) "添加头像" else "更换头像"
                        )
                    }
                }
            }

            // 基本信息卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 卡片标题栏
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isBasicInfoExpanded = !isBasicInfoExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "基本信息",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isBasicInfoExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isBasicInfoExpanded) "收起" else "展开",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // 可折叠内容
                    AnimatedVisibility(
                        visible = isBasicInfoExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("姓名 *") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = name.isBlank(),
                                supportingText = if (name.isBlank()) {
                                    { Text("姓名不能为空") }
                                } else null
                            )
                            
                            OutlinedTextField(
                                value = source,
                                onValueChange = { source = it },
                                label = { Text("来源 *") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = source.isBlank(),
                                supportingText = if (source.isBlank()) {
                                    { Text("来源不能为空") }
                                } else null,
                                placeholder = { Text("例如：x群群友，xx公司同事等") }
                            )

                            OutlinedTextField(
                                value = realName,
                                onValueChange = { realName = it },
                                label = { Text("真实姓名") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("如果与显示姓名不同") }
                            )
                        }
                    }
                }
            }

            // 生日信息卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isBirthdayExpanded = !isBirthdayExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "生日信息",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isBirthdayExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isBirthdayExpanded) "收起" else "展开",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    AnimatedVisibility(
                        visible = isBirthdayExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = birthYear,
                                onValueChange = { 
                                    if (it.isEmpty() || (it.toIntOrNull() != null && it.length <= 4)) {
                                        birthYear = it
                                    }
                                },
                                label = { Text("年") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("1990") }
                            )
                            
                            OutlinedTextField(
                                value = birthMonth,
                                onValueChange = { 
                                    val num = it.toIntOrNull()
                                    if (it.isEmpty() || (num != null && num in 1..12)) {
                                        birthMonth = it
                                    }
                                },
                                label = { Text("月") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("1-12") }
                            )
                            
                            OutlinedTextField(
                                value = birthDay,
                                onValueChange = { 
                                    val num = it.toIntOrNull()
                                    if (it.isEmpty() || (num != null && num in 1..31)) {
                                        birthDay = it
                                    }
                                },
                                label = { Text("日") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("1-31") }
                            )
                        }
                    }
                }
            }

            // 昵称卡片
            CollapsibleDetailCard(
                title = "称呼",
                isExpanded = isNicknamesExpanded,
                onToggle = { isNicknamesExpanded = !isNicknamesExpanded },
                items = nicknames,
                onAddItem = { nicknames.add("") },
                onUpdateItem = { index, value -> nicknames[index] = value },
                onRemoveItem = { index -> nicknames.removeAt(index) },
                placeholder = "昵称、别名等"
            )

            // 联系方式卡片
            CollapsibleContactCard(
                title = "联系方式",
                isExpanded = isContactsExpanded,
                onToggle = { isContactsExpanded = !isContactsExpanded },
                contacts = contacts,
                onAddContact = { type, value -> contacts[type] = value },
                onRemoveContact = { type -> contacts.remove(type) }
            )

            // 爱好卡片
            CollapsibleDetailCard(
                title = "爱好",
                isExpanded = isHobbiesExpanded,
                onToggle = { isHobbiesExpanded = !isHobbiesExpanded },
                items = hobbies,
                onAddItem = { hobbies.add("") },
                onUpdateItem = { index, value -> hobbies[index] = value },
                onRemoveItem = { index -> hobbies.removeAt(index) },
                placeholder = "兴趣爱好"
            )

            // 雷点卡片
            CollapsibleDetailCard(
                title = "雷点",
                isExpanded = isDislikesExpanded,
                onToggle = { isDislikesExpanded = !isDislikesExpanded },
                items = dislikes,
                onAddItem = { dislikes.add("") },
                onUpdateItem = { index, value -> dislikes[index] = value },
                onRemoveItem = { index -> dislikes.removeAt(index) },
                placeholder = "不喜欢的事物"
            )

            // 特征卡片
            CollapsibleDetailCard(
                title = "特点",
                isExpanded = isTraitsExpanded,
                onToggle = { isTraitsExpanded = !isTraitsExpanded },
                items = traits,
                onAddItem = { traits.add("") },
                onUpdateItem = { index, value -> traits[index] = value },
                onRemoveItem = { index -> traits.removeAt(index) },
                placeholder = "性格外貌特征、特长等"
            )

            // 地址卡片
            CollapsibleDetailCard(
                title = "地址",
                isExpanded = isAddressesExpanded,
                onToggle = { isAddressesExpanded = !isAddressesExpanded },
                items = addresses,
                onAddItem = { addresses.add("") },
                onUpdateItem = { index, value -> addresses[index] = value },
                onRemoveItem = { index -> addresses.removeAt(index) },
                placeholder = "家庭地址、工作地址等"
            )

            // 备注卡片
            CollapsibleDetailCard(
                title = "备注",
                isExpanded = isNotesExpanded,
                onToggle = { isNotesExpanded = !isNotesExpanded },
                items = notes,
                onAddItem = { notes.add("") },
                onUpdateItem = { index, value -> notes[index] = value },
                onRemoveItem = { index -> notes.removeAt(index) },
                placeholder = "其他备注信息",
                isMultiLine = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddContactScreenPreview() {
    AddContactScreen()
}

