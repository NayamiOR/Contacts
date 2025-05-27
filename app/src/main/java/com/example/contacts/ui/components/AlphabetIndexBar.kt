package com.example.contacts.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// 字母索引条组件
@Composable
fun AlphabetIndexBar(
    modifier: Modifier = Modifier.Companion,
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
            modifier = Modifier.Companion
                .fillMaxHeight()
                .width(32.dp)
                .align(Alignment.Companion.CenterEnd) // 固定在右侧
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
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
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
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            alphabetIndex.forEach { letter ->
                val isSelected = isDragging && currentSelectedLetter == letter

                Box(
                    modifier = Modifier.Companion
                        .size(20.dp)
                        .background(
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                Color.Companion.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = if (isSelected) 12.sp else 10.sp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Companion.Bold else FontWeight.Companion.Normal,
                        textAlign = TextAlign.Companion.Center
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
            modifier = Modifier.Companion
                .align(Alignment.Companion.TopStart)
                .padding(start = 8.dp) // 与左边缘的距离
        ) {
            Card(
                modifier = Modifier.Companion.size(40.dp), // 进一步缩小气泡尺寸
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(30)// 使用圆形气泡
            ) {
                Box(
                    modifier = Modifier.Companion.fillMaxSize(),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Text(
                        text = currentSelectedLetter?.toString() ?: "",
                        style = MaterialTheme.typography.titleMedium, // 调整字体大小
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Companion.Bold
                    )
                }
            }
        }
    }
}