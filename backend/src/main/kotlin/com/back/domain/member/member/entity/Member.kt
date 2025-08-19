package com.back.domain.member.member.entity

import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

@Entity
class Member(
    id: Int,
    @field:Column(unique = true) val username: String,
    var password: String? = null,
    var nickname: String,
    @field:Column(unique = true) var apiKey: String,
    var profileImgUrl: String? = null,
) : BaseEntity(id) {
    // JPA와 상관없는 객체를 만들 때 사용, SecurityUser로부터 정보를 받아와서 생성할 때 사용
    constructor(id: Int, username: String, nickname: String) : this(
        id,
        username,
        null,
        nickname,
        ""
    )

    constructor(username: String, password: String?, nickname: String, profileImgUrl: String?) : this(
        0,
        username,
        password,
        nickname,
        UUID.randomUUID().toString(), // apiKey는 UUID로 생성
        profileImgUrl
    )

    val profileImgUrlOrDefault: String
        get() {
            profileImgUrl?.let { return it }

            return "https://placehold.co/600x600?text=U_U"
        }

    val name: String
        get() = nickname

    val isAdmin: Boolean
        get() {
            if ("system" == username) return true
            if ("admin" == username) return true

            return false
        }

    val authoritiesAsStringList: List<String>
        get() {
            val authorities = mutableListOf<String>()

            if (isAdmin) authorities.add("ROLE_ADMIN")

            return authorities
        }

    val authorities: Collection<GrantedAuthority>
        get() = authoritiesAsStringList.map { SimpleGrantedAuthority(it) }

    fun modify(nickname: String, profileImgUrl: String?) {
        this.nickname = nickname
        this.profileImgUrl = profileImgUrl
    }

    fun modifyApiKey(apiKey: String) {
        this.apiKey = apiKey
    }
}
