package com.back.global.app

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "custom")
class CustomConfigProperties {
    lateinit var notProdMembers: MutableList<NotProdMember>

    data class NotProdMember(
        val username: String,
        val apiKey: String,
        val nickname: String,
        val profileImgUrl: String
    )
}
