package com.example.contacts.ui.components

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// 可折叠的分组标题组件
@Composable
fun CollapsibleGroupSectionHeader(
    groupName: String,
    groupColor: String,
    contactCount: Int,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit
) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onToggleCollapse() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            // 分组颜色指示器
            Box(
                modifier = Modifier.Companion
                    .size(16.dp)
                    .background(
                        color = try {
                            androidx.compose.ui.graphics.Color(Color.parseColor(groupColor))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        },
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.Companion.width(12.dp))

            // 分组名称
            Text(
                text = groupName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Companion.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.Companion.weight(1f)
            )

            // 联系人数量
            Text(
                text = "$contactCount 人",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.Companion.padding(end = 8.dp)
            )

            // 折叠/展开图标
            Icon(
                imageVector = if (isCollapsed) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isCollapsed) "展开分组" else "折叠分组",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}