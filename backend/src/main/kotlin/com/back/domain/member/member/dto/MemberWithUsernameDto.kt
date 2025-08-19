package com.back.domain.member.member.dto

import com.back.domain.member.member.entity.Member
import java.time.LocalDateTime

data class MemberWithUsernameDto(
    val id: Int,
    val createDate: LocalDateTime,
    val modifyDate: LocalDateTime,
    val isAdmin: Boolean,
    val username: String,
    val name: String,
    val profileImageUrl: String,
) {
    constructor(member: Member) : this(
        id = member.id,
        createDate = member.createDate,
        modifyDate = member.modifyDate,
        isAdmin = member.isAdmin,
        username = member.username,
        name = member.name,
        profileImageUrl = member.profileImgUrlOrDefault
    )
}
