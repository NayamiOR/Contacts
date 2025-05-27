package com.example.contacts.data.entities

// 联系人与分组的关联数据类
data class ContactWithGroup(
    val contact: Contact,
    val group: ContactGroup?
)

// 用于查询结果的数据类
data class ContactWithGroupInfo(
    val id: Long,
    val name: String,
    val source: String,
    val birthYear: Int?,
    val birthMonth: Int?,
    val birthDay: Int?,
    val realName: String?,
    val phone: String?,
    val groupId: Long?,
    val details: String?,
    val groupName: String?,
    val groupColor: String?
) 