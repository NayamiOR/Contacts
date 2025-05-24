package com.example.contacts.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CollapsibleContactCard(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    contacts: SnapshotStateMap<String, String>,
    onAddContact: (String, String) -> Unit,
    onRemoveContact: (String) -> Unit
) {
    var newContactType by remember { mutableStateOf("") }
    var newContactValue by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.Companion.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Text(
                    text = title,
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
                Column(
                    modifier = Modifier.Companion.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contacts.forEach { (type, value) ->
                        Row(
                            modifier = Modifier.Companion.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Companion.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = type,
                                onValueChange = { },
                                modifier = Modifier.Companion.weight(1f),
                                label = { Text("类型") },
                                enabled = false
                            )
                            OutlinedTextField(
                                value = value,
                                onValueChange = { onAddContact(type, it) },
                                modifier = Modifier.Companion.weight(2f),
                                label = { Text("联系方式") }
                            )
                            IconButton(onClick = { onRemoveContact(type) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // 添加新联系方式
                    Row(
                        modifier = Modifier.Companion.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Companion.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newContactType,
                            onValueChange = { newContactType = it },
                            modifier = Modifier.Companion.weight(1f),
                            placeholder = { Text("邮箱、钉钉等") },
                            label = { Text("类型") }
                        )
                        OutlinedTextField(
                            value = newContactValue,
                            onValueChange = { newContactValue = it },
                            modifier = Modifier.Companion.weight(2f),
                            placeholder = { Text("联系方式") },
                            label = { Text("联系方式") }
                        )
                        IconButton(
                            onClick = {
                                if (newContactType.isNotBlank() && newContactValue.isNotBlank()) {
                                    onAddContact(newContactType, newContactValue)
                                    newContactType = ""
                                    newContactValue = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "添加",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}