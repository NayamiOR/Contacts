package com.example.contacts.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.contacts.data.entities.Contact
import com.example.contacts.data.entities.ContactDetails
import com.example.contacts.data.entities.ContactGroup
import com.example.contacts.data.utils.parseDetails
import androidx.compose.material.icons.filled.Check

// 联系人表单状态数据类
@SuppressLint("MutableCollectionMutableState")
class ContactFormState {
    var name by mutableStateOf("")
    var source by mutableStateOf("")
    var birthYear by mutableStateOf("")
    var birthMonth by mutableStateOf("")
    var birthDay by mutableStateOf("")
    var realName by mutableStateOf("")
    var avatarUri by mutableStateOf<String?>(null)
    val nicknames: SnapshotStateList<String> = mutableStateListOf()
    val contacts: SnapshotStateMap<String, String> = mutableStateMapOf(
        "微信" to "",
        "QQ" to "",
        "手机号" to ""
    )
    val hobbies: SnapshotStateList<String> = mutableStateListOf()
    val dislikes: SnapshotStateList<String> = mutableStateListOf()
    val traits: SnapshotStateList<String> = mutableStateListOf()
    val addresses: SnapshotStateList<String> = mutableStateListOf()
    val notes: SnapshotStateList<String> = mutableStateListOf()
    val customFields: SnapshotStateMap<String, MutableList<String>> = mutableStateMapOf()
}

// 折叠状态数据类
class CollapsibleState {
    var isBasicInfoExpanded by mutableStateOf(true)
    var isBirthdayExpanded by mutableStateOf(false)
    var isNicknamesExpanded by mutableStateOf(false)
    var isContactsExpanded by mutableStateOf(false)
    var isHobbiesExpanded by mutableStateOf(false)
    var isDislikesExpanded by mutableStateOf(false)
    var isTraitsExpanded by mutableStateOf(false)
    var isAddressesExpanded by mutableStateOf(false)
    var isNotesExpanded by mutableStateOf(false)
    var isCustomFieldsExpanded by mutableStateOf(false)
}

// 从Contact对象初始化表单状态
fun initializeFormStateFromContact(contact: Contact): ContactFormState {
    val details = parseDetails(contact.details)
    
    return ContactFormState().apply {
        name = contact.name
        source = contact.source
        birthYear = contact.birthYear?.toString() ?: ""
        birthMonth = contact.birthMonth?.toString() ?: ""
        birthDay = contact.birthDay?.toString() ?: ""
        realName = contact.realName ?: ""
        
        // 初始化详细信息
        details?.let { d ->
            d.nicknames?.let { nicknames.addAll(it) }
            d.contacts?.let { contacts.putAll(it) }
            d.hobbies?.let { hobbies.addAll(it) }
            d.dislikes?.let { dislikes.addAll(it) }
            d.traits?.let { traits.addAll(it) }
            d.addresses?.let { addresses.addAll(it) }
            d.notes?.let { notes.addAll(it) }
            d.custom?.let { custom ->
                custom.forEach { (key, values) ->
                    customFields[key] = values.toMutableList()
                }
            }
        }
    }
}

// 从表单状态创建ContactDetails
fun createContactDetailsFromFormState(formState: ContactFormState): ContactDetails {
    return ContactDetails(
        nicknames = formState.nicknames.filter { it.isNotBlank() },
        contacts = formState.contacts.filterValues { it.isNotBlank() },
        hobbies = formState.hobbies.filter { it.isNotBlank() },
        dislikes = formState.dislikes.filter { it.isNotBlank() },
        traits = formState.traits.filter { it.isNotBlank() },
        addresses = formState.addresses.filter { it.isNotBlank() },
        notes = formState.notes.filter { it.isNotBlank() },
        custom = formState.customFields.mapValues { it.value.filter { item -> item.isNotBlank() } }
    )
}

@Composable
fun ContactFormContent(
    formState: ContactFormState,
    collapsibleState: CollapsibleState,
    groups: List<ContactGroup> = emptyList(),
    selectedGroupId: Long? = null,
    onGroupSelected: (Long?) -> Unit = {},
    onGroupAdded: ((ContactGroup) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 头像区域卡片
        AvatarCard(
            avatarUri = formState.avatarUri,
            onAvatarChange = { formState.avatarUri = it }
        )

        // 基本信息卡片
        BasicInfoCard(
            name = formState.name,
            onNameChange = { formState.name = it },
            source = formState.source,
            onSourceChange = { formState.source = it },
            realName = formState.realName,
            onRealNameChange = { formState.realName = it },
            groups = groups,
            selectedGroupId = selectedGroupId,
            onGroupSelected = onGroupSelected,
            onGroupAdded = onGroupAdded,
            isExpanded = collapsibleState.isBasicInfoExpanded,
            onToggle = {
                collapsibleState.isBasicInfoExpanded = !collapsibleState.isBasicInfoExpanded
            }
        )

        // 生日信息卡片
        BirthdayCard(
            birthYear = formState.birthYear,
            onBirthYearChange = { formState.birthYear = it },
            birthMonth = formState.birthMonth,
            onBirthMonthChange = { formState.birthMonth = it },
            birthDay = formState.birthDay,
            onBirthDayChange = { formState.birthDay = it },
            isExpanded = collapsibleState.isBirthdayExpanded,
            onToggle = {
                collapsibleState.isBirthdayExpanded = !collapsibleState.isBirthdayExpanded
            }
        )

        // 昵称卡片
        CollapsibleDetailCard(
            title = "称呼",
            isExpanded = collapsibleState.isNicknamesExpanded,
            onToggle = {
                collapsibleState.isNicknamesExpanded = !collapsibleState.isNicknamesExpanded
            },
            items = formState.nicknames,
            onAddItem = { formState.nicknames.add("") },
            onUpdateItem = { index, value -> formState.nicknames[index] = value },
            onRemoveItem = { index -> formState.nicknames.removeAt(index) },
            placeholder = "昵称、别名等"
        )

        // 联系方式卡片
        CollapsibleContactCard(
            title = "联系方式",
            isExpanded = collapsibleState.isContactsExpanded,
            onToggle = {
                collapsibleState.isContactsExpanded = !collapsibleState.isContactsExpanded
            },
            contacts = formState.contacts,
            onAddContact = { type, value -> formState.contacts[type] = value },
            onRemoveContact = { type -> formState.contacts.remove(type) }
        )

        // 爱好卡片
        CollapsibleDetailCard(
            title = "爱好",
            isExpanded = collapsibleState.isHobbiesExpanded,
            onToggle = { collapsibleState.isHobbiesExpanded = !collapsibleState.isHobbiesExpanded },
            items = formState.hobbies,
            onAddItem = { formState.hobbies.add("") },
            onUpdateItem = { index, value -> formState.hobbies[index] = value },
            onRemoveItem = { index -> formState.hobbies.removeAt(index) },
            placeholder = "兴趣爱好"
        )

        // 雷点卡片
        CollapsibleDetailCard(
            title = "雷点",
            isExpanded = collapsibleState.isDislikesExpanded,
            onToggle = {
                collapsibleState.isDislikesExpanded = !collapsibleState.isDislikesExpanded
            },
            items = formState.dislikes,
            onAddItem = { formState.dislikes.add("") },
            onUpdateItem = { index, value -> formState.dislikes[index] = value },
            onRemoveItem = { index -> formState.dislikes.removeAt(index) },
            placeholder = "不喜欢的事物"
        )

        // 特征卡片
        CollapsibleDetailCard(
            title = "特点",
            isExpanded = collapsibleState.isTraitsExpanded,
            onToggle = { collapsibleState.isTraitsExpanded = !collapsibleState.isTraitsExpanded },
            items = formState.traits,
            onAddItem = { formState.traits.add("") },
            onUpdateItem = { index, value -> formState.traits[index] = value },
            onRemoveItem = { index -> formState.traits.removeAt(index) },
            placeholder = "性格外貌特征、特长等"
        )

        // 地址卡片
        CollapsibleDetailCard(
            title = "地址",
            isExpanded = collapsibleState.isAddressesExpanded,
            onToggle = {
                collapsibleState.isAddressesExpanded = !collapsibleState.isAddressesExpanded
            },
            items = formState.addresses,
            onAddItem = { formState.addresses.add("") },
            onUpdateItem = { index, value -> formState.addresses[index] = value },
            onRemoveItem = { index -> formState.addresses.removeAt(index) },
            placeholder = "家庭地址、工作地址等"
        )

        // 备注卡片
        CollapsibleDetailCard(
            title = "备注",
            isExpanded = collapsibleState.isNotesExpanded,
            onToggle = { collapsibleState.isNotesExpanded = !collapsibleState.isNotesExpanded },
            items = formState.notes,
            onAddItem = { formState.notes.add("") },
            onUpdateItem = { index, value -> formState.notes[index] = value },
            onRemoveItem = { index -> formState.notes.removeAt(index) },
            placeholder = "其他备注信息",
            isMultiLine = true
        )
    }
}

@Composable
private fun AvatarCard(
    avatarUri: String?,
    onAvatarChange: (String?) -> Unit
) {
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
}

@Composable
private fun BasicInfoCard(
    name: String,
    onNameChange: (String) -> Unit,
    source: String,
    onSourceChange: (String) -> Unit,
    realName: String,
    onRealNameChange: (String) -> Unit,
    groups: List<ContactGroup> = emptyList(),
    selectedGroupId: Long? = null,
    onGroupSelected: (Long?) -> Unit = {},
    onGroupAdded: ((ContactGroup) -> Unit)? = null,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
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
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "基本信息",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // 可折叠内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = { Text("姓名 *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = name.isBlank(),
                        supportingText = if (name.isBlank()) {
                            { Text("姓名不能为空") }
                        } else null
                    )

                    OutlinedTextField(
                        value = source,
                        onValueChange = onSourceChange,
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
                        onValueChange = onRealNameChange,
                        label = { Text("真实姓名") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("如果与显示姓名不同") }
                    )

                    // 分组选择
                    GroupSelector(
                        groups = groups,
                        selectedGroupId = selectedGroupId,
                        onGroupSelected = onGroupSelected,
                        onGroupAdded = onGroupAdded
                    )
                }
            }
        }
    }
}

@Composable
private fun BirthdayCard(
    birthYear: String,
    onBirthYearChange: (String) -> Unit,
    birthMonth: String,
    onBirthMonthChange: (String) -> Unit,
    birthDay: String,
    onBirthDayChange: (String) -> Unit,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
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
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "生日信息",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
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
                                onBirthYearChange(it)
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
                                onBirthMonthChange(it)
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
                                onBirthDayChange(it)
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
}

// 分组选择器组件 - 支持新增分组
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GroupSelector(
    groups: List<ContactGroup>,
    selectedGroupId: Long?,
    onGroupSelected: (Long?) -> Unit,
    onGroupAdded: ((ContactGroup) -> Unit)? = null // 新增分组回调
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var newGroupColor by remember { mutableStateOf("#2196F3") }
    
    val selectedGroup = groups.find { it.id == selectedGroupId }
    
    // 预定义的颜色选项
    val colorOptions = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7",
        "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
        "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
        "#FFEB3B", "#FFC107", "#FF9800", "#FF5722",
        "#795548", "#9E9E9E", "#607D8B"
    )
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedGroup?.name ?: "未分组",
            onValueChange = { },
            readOnly = true,
            label = { Text("分组") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // 新增分组选项
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "新增分组",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "新增分组",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                onClick = {
                    expanded = false
                    showAddGroupDialog = true
                }
            )
            
            // 分隔线
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            // 未分组选项
            DropdownMenuItem(
                text = { Text("未分组") },
                onClick = {
                    onGroupSelected(null)
                    expanded = false
                },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = Color.Gray,
                                shape = CircleShape
                            )
                    )
                }
            )
            
            // 各个分组选项
            groups.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group.name) },
                    onClick = {
                        onGroupSelected(group.id)
                        expanded = false
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = try {
                                        Color(android.graphics.Color.parseColor(group.color))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                )
            }
        }
    }
    
    // 新增分组对话框
    if (showAddGroupDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddGroupDialog = false
                newGroupName = ""
                newGroupColor = "#2196F3"
            },
            title = { Text("新增分组") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 分组名称输入
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("分组名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = newGroupName.isBlank(),
                        supportingText = if (newGroupName.isBlank()) {
                            { Text("分组名称不能为空") }
                        } else null
                    )
                    
                    // 颜色选择
                    Text(
                        text = "选择颜色",
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    // 颜色选择网格
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = Color(android.graphics.Color.parseColor(color)),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (newGroupColor == color) 3.dp else 1.dp,
                                        color = if (newGroupColor == color) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        newGroupColor = color
                                    }
                            ) {
                                if (newGroupColor == color) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "已选择",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newGroupName.isNotBlank()) {
                            val newGroup = ContactGroup(
                                name = newGroupName.trim(),
                                color = newGroupColor
                            )
                            onGroupAdded?.invoke(newGroup)
                            showAddGroupDialog = false
                            newGroupName = ""
                            newGroupColor = "#2196F3"
                        }
                    },
                    enabled = newGroupName.isNotBlank()
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddGroupDialog = false
                        newGroupName = ""
                        newGroupColor = "#2196F3"
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
} 