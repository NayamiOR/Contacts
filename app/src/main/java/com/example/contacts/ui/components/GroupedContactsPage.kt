package com.example.contacts.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.contacts.Contact
import com.example.contacts.ContactGroup

// 按分组显示的联系人页面 - 支持折叠
@Composable
fun GroupedContactsPage(
    groupedContactsByGroup: Map<ContactGroup?, List<Contact>>,
    searchQuery: String,
    isSelectionMode: Boolean,
    selectedContacts: Set<Long>,
    collapsedGroups: Set<Long>,
    onToggleGroupCollapse: (Long?) -> Unit,
    onContactClick: (Contact) -> Unit,
    onContactLongClick: (Contact) -> Unit,
    onEditContactClick: (String) -> Unit,
    onDeleteContactClick: (Contact) -> Unit
) {
    // 过滤搜索结果
    val filteredGroupedContacts = remember(groupedContactsByGroup, searchQuery) {
        if (searchQuery.isBlank()) {
            groupedContactsByGroup
        } else {
            groupedContactsByGroup.mapValues { (_, contacts) ->
                contacts.filter { contact ->
                    contact.name.contains(searchQuery, ignoreCase = true) ||
                            contact.source.contains(searchQuery, ignoreCase = true) ||
                            contact.realName?.contains(searchQuery, ignoreCase = true) == true
                }
            }.filterValues { it.isNotEmpty() }
        }
    }

    when {
        filteredGroupedContacts.isEmpty() -> {
            Box(
                modifier = Modifier.Companion.fillMaxSize(),
                contentAlignment = Alignment.Companion.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.Companion.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "无联系人",
                        modifier = Modifier.Companion.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (searchQuery.isBlank()) "还没有联系人" else "未找到匹配的联系人",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.Companion.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                filteredGroupedContacts.forEach { (group, contactsInGroup) ->
                    val groupId = group?.id ?: -1L // 未分组使用特殊ID -1
                    val isCollapsed = collapsedGroups.contains(groupId)

                    // 分组标题
                    item(key = "group_header_${group?.id ?: "ungrouped"}") {
                        CollapsibleGroupSectionHeader(
                            groupName = group?.name ?: "未分组",
                            groupColor = group?.color ?: "#9E9E9E",
                            contactCount = contactsInGroup.size,
                            isCollapsed = isCollapsed,
                            onToggleCollapse = { onToggleGroupCollapse(group?.id) }
                        )
                    }

                    // 该分组下的联系人（只有在未折叠时才显示）
                    if (!isCollapsed) {
                        items(
                            items = contactsInGroup,
                            key = { contact -> "group_${group?.id ?: "ungrouped"}_${contact.id}" }
                        ) { contact ->
                            ContactItem(
                                contact = contact,
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedContacts.contains(contact.id),
                                onClick = { onContactClick(contact) },
                                onLongClick = { onContactLongClick(contact) },
                                onEditClick = { onEditContactClick(contact.id.toString()) },
                                onDeleteClick = { onDeleteContactClick(contact) }
                            )
                        }
                    }
                }
            }
        }
    }
}