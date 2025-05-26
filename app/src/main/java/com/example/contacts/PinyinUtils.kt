package com.example.contacts

import java.text.Collator
import java.util.Locale

/**
 * 拼音转换工具类
 * 用于处理中文字符的拼音排序
 */
object PinyinUtils {
    
    // 中文排序器
    private val chineseCollator = Collator.getInstance(Locale.CHINESE).apply {
        strength = Collator.PRIMARY
    }
    
    // 常用汉字拼音首字母映射表（部分常用字）
    private val pinyinMap = mapOf(
        // A
        '阿' to 'A', '啊' to 'A', '爱' to 'A', '安' to 'A', '按' to 'A',
        // B
        '白' to 'B', '百' to 'B', '北' to 'B', '本' to 'B', '比' to 'B', '边' to 'B', '别' to 'B', '不' to 'B',
        // C
        '才' to 'C', '从' to 'C', '出' to 'C', '成' to 'C', '长' to 'C', '常' to 'C', '车' to 'C', '陈' to 'C',
        // D
        '大' to 'D', '到' to 'D', '的' to 'D', '地' to 'D', '第' to 'D', '点' to 'D', '东' to 'D', '都' to 'D',
        // E
        '而' to 'E', '二' to 'E', '儿' to 'E',
        // F
        '发' to 'F', '法' to 'F', '方' to 'F', '分' to 'F', '风' to 'F', '服' to 'F', '福' to 'F',
        // G
        '个' to 'G', '给' to 'G', '跟' to 'G', '工' to 'G', '公' to 'G', '国' to 'G', '过' to 'G',
        // H
        '好' to 'H', '和' to 'H', '很' to 'H', '后' to 'H', '回' to 'H', '会' to 'H', '黄' to 'H', '华' to 'H',
        // J
        '家' to 'J', '见' to 'J', '叫' to 'J', '就' to 'J', '进' to 'J', '经' to 'J', '金' to 'J', '今' to 'J',
        // K
        '可' to 'K', '看' to 'K', '开' to 'K', '快' to 'K',
        // L
        '来' to 'L', '了' to 'L', '老' to 'L', '李' to 'L', '里' to 'L', '两' to 'L', '刘' to 'L', '路' to 'L',
        // M
        '没' to 'M', '么' to 'M', '们' to 'M', '面' to 'M', '明' to 'M', '马' to 'M', '毛' to 'M',
        // N
        '那' to 'N', '你' to 'N', '年' to 'N', '能' to 'N', '南' to 'N', '女' to 'N',
        // P
        '朋' to 'P', '片' to 'P', '平' to 'P',
        // Q
        '去' to 'Q', '前' to 'Q', '钱' to 'Q', '请' to 'Q', '清' to 'Q', '全' to 'Q',
        // R
        '人' to 'R', '让' to 'R', '如' to 'R', '日' to 'R',
        // S
        '是' to 'S', '说' to 'S', '上' to 'S', '时' to 'S', '生' to 'S', '什' to 'S', '三' to 'S', '孙' to 'S',
        // T
        '他' to 'T', '她' to 'T', '太' to 'T', '天' to 'T', '听' to 'T', '同' to 'T', '头' to 'T',
        // W
        '我' to 'W', '为' to 'W', '王' to 'W', '问' to 'W', '文' to 'W', '五' to 'W', '万' to 'W',
        // X
        '小' to 'X', '想' to 'X', '下' to 'X', '先' to 'X', '现' to 'X', '新' to 'X', '学' to 'X', '许' to 'X',
        // Y
        '一' to 'Y', '有' to 'Y', '也' to 'Y', '要' to 'Y', '用' to 'Y', '又' to 'Y', '月' to 'Y', '杨' to 'Y',
        // Z
        '在' to 'Z', '这' to 'Z', '中' to 'Z', '只' to 'Z', '知' to 'Z', '张' to 'Z', '赵' to 'Z', '周' to 'Z'
    )
    
    /**
     * 获取字符串的首字母，用于分组
     * @param text 输入文本
     * @return 首字母（A-Z或#）
     */
    fun getFirstLetter(text: String): Char {
        if (text.isEmpty()) return '#'
        
        val firstChar = text.first()
        
        // 如果是英文字母，直接返回大写
        if (firstChar.isLetter() && firstChar.code < 128) {
            return firstChar.uppercaseChar()
        }
        
        // 如果是中文字符，先查找映射表
        pinyinMap[firstChar]?.let { return it }
        
        // 如果映射表中没有，使用Unicode范围估算
        if (isChinese(firstChar)) {
            return getPinyinFirstLetterByUnicode(firstChar)
        }
        
        // 其他字符返回#
        return '#'
    }
    
    /**
     * 判断字符是否为中文字符
     */
    private fun isChinese(char: Char): Boolean {
        val code = char.code
        return code in 0x4E00..0x9FFF
    }
    
    /**
     * 通过Unicode范围估算拼音首字母
     */
    private fun getPinyinFirstLetterByUnicode(char: Char): Char {
        val code = char.code
        return when {
            code < 0x4F00 -> 'A'
            code < 0x5200 -> 'B'
            code < 0x5400 -> 'C'
            code < 0x5600 -> 'D'
            code < 0x5800 -> 'F'
            code < 0x5A00 -> 'G'
            code < 0x5C00 -> 'H'
            code < 0x5E00 -> 'J'
            code < 0x6000 -> 'K'
            code < 0x6200 -> 'L'
            code < 0x6400 -> 'M'
            code < 0x6600 -> 'N'
            code < 0x6800 -> 'P'
            code < 0x6A00 -> 'Q'
            code < 0x6C00 -> 'R'
            code < 0x6E00 -> 'S'
            code < 0x7000 -> 'T'
            code < 0x7200 -> 'W'
            code < 0x7400 -> 'X'
            code < 0x7600 -> 'Y'
            else -> 'Z'
        }
    }
    
    /**
     * 比较两个字符串，支持中文拼音排序
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 比较结果
     */
    fun compareStrings(str1: String, str2: String): Int {
        return chineseCollator.compare(str1, str2)
    }
    
    /**
     * 获取排序键，用于排序
     * @param text 输入文本
     * @return 排序键
     */
    fun getSortKey(text: String): String {
        return buildString {
            // 首先按首字母分组
            append(getFirstLetter(text))
            append("_")
            // 然后使用原始文本进行二级排序
            append(text)
        }
    }
} 