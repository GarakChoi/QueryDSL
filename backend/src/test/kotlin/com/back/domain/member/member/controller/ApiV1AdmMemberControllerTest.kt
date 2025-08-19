package com.back.domain.member.member.controller

import com.back.domain.member.member.service.MemberService
import com.back.standard.extensions.getOrThrow
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1AdmMemberControllerTest {
    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("다건조회")
    @WithUserDetails("admin")
    fun t1() {
        val resultActions = mvc
            .perform(
                get("/api/v1/adm/members")
            )
            .andDo(print())

        val members = memberService.findAll()

        resultActions
            .andExpect(handler().handlerType(ApiV1AdmMemberController::class.java))
            .andExpect(handler().methodName("getItems"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(members.size))

        for (i in members.indices) {
            val member = members[i]
            resultActions
                .andExpect(jsonPath("$[$i].id").value(member.id))
                .andExpect(
                    jsonPath("$[$i].createDate")
                        .value(Matchers.startsWith(member.createDate.toString().take(20)))
                )
                .andExpect(
                    jsonPath("$[$i].modifyDate")
                        .value(Matchers.startsWith(member.modifyDate.toString().take(20)))
                )
                .andExpect(jsonPath("$[$i].name").value(member.name))
                .andExpect(jsonPath("$[$i].username").value(member.username))
                .andExpect(jsonPath("$[$i].isAdmin").value(member.isAdmin))
        }
    }

    @Test
    @DisplayName("다건조회, without permission")
    @WithUserDetails("user1")
    fun t3() {
        val resultActions = mvc
            .perform(
                get("/api/v1/adm/members")
            )
            .andDo(print())

        resultActions
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("권한이 없습니다."))
    }

    @Test
    @DisplayName("단건조회")
    @WithUserDetails("admin")
    fun t2() {
        val id = 1

        val resultActions = mvc
            .perform(
                get("/api/v1/adm/members/$id")
            )
            .andDo(print())

        val member = memberService.findById(id).getOrThrow()

        resultActions
            .andExpect(handler().handlerType(ApiV1AdmMemberController::class.java))
            .andExpect(handler().methodName("getItem"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(member.id))
            .andExpect(
                jsonPath("$.createDate")
                    .value(Matchers.startsWith(member.createDate.toString().take(20)))
            )
            .andExpect(
                jsonPath("$.modifyDate")
                    .value(Matchers.startsWith(member.modifyDate.toString().take(20)))
            )
            .andExpect(jsonPath("$.name").value(member.name))
            .andExpect(jsonPath("$.username").value(member.username))
            .andExpect(jsonPath("$.isAdmin").value(member.isAdmin))
    }

    @Test
    @DisplayName("단건조회, without permission")
    @WithUserDetails("user1")
    fun t4() {
        val id = 1

        val resultActions = mvc
            .perform(
                get("/api/v1/adm/members/$id")
            )
            .andDo(print())

        resultActions
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("권한이 없습니다."))
    }
}