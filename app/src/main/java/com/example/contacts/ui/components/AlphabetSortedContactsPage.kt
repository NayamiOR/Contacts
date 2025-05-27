package com.example.contacts.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.contacts.Contact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// 按字母排序的联系人页面
@Composable
fun AlphabetSortedContactsPage(
    filteredContacts: List<Contact>,
    groupedContacts: Map<Char, List<Contact>>,
    alphabetIndex: List<Char>,
    letterToIndex: Map<Char, Int>,
    listState: LazyListState,
    searchQuery: String,
    isSelectionMode: Boolean,
    selectedContacts: Set<Long>,
    onContactClick: (Contact) -> Unit,
    onContactLongClick: (Contact) -> Unit,
    onEditContactClick: (String) -> Unit,
    onDeleteContactClick: (Contact) -> Unit,
    scope: CoroutineScope
) {
    when {
        filteredContacts.isEmpty() -> {
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
            Box(modifier = Modifier.Companion.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.Companion.fillMaxSize(),
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
                                onClick = { onContactClick(contact) },
                                onLongClick = { onContactLongClick(contact) },
                                onEditClick = { onEditContactClick(contact.id.toString()) },
                                onDeleteClick = { onDeleteContactClick(contact) }
                            )
                        }
                    }
                }

                // 字母索引条
                if (searchQuery.isBlank() && groupedContacts.isNotEmpty()) {
                    AlphabetIndexBar(
                        modifier = Modifier.Companion.align(Alignment.Companion.CenterEnd),
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

// 分组标题组件
@Composable
fun SectionHeader(letter: Char) {
    Box(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = letter.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Companion.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}