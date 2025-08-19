package com.back.domain.member.member.controller

import com.back.domain.member.member.service.MemberService
import com.back.standard.extensions.getOrThrow
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1MemberControllerTest {
    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("회원가입")
    fun t1() {
        val resultActions = mvc
            .perform(
                post("/api/v1/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "username": "usernew",
                            "password": "1234",
                            "nickname": "무명"
                        }
                    """
                    )
            )
            .andDo(print())

        val member = memberService.findByUsername("usernew").getOrThrow()

        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("join"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.resultCode").value("201-1"))
            .andExpect(jsonPath("$.msg").value("${member.name}님 환영합니다. 회원가입이 완료되었습니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.id").value(member.id))
            .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(member.createDate.toString().take(20))))
            .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(member.modifyDate.toString().take(20))))
            .andExpect(jsonPath("$.data.name").value(member.name))
            .andExpect(jsonPath("$.data.isAdmin").value(member.isAdmin))
    }

    @Test
    @DisplayName("로그인")
    fun t2() {
        val resultActions = mvc
            .perform(
                post("/api/v1/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "username": "user1",
                            "password": "1234"
                        }
                    """
                    )
            )
            .andDo(print())

        val member = memberService.findByUsername("user1").getOrThrow()

        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("login"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${member.nickname}님 환영합니다."))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.item").exists())
            .andExpect(jsonPath("$.data.item.id").value(member.id))
            .andExpect(
                jsonPath("$.data.item.createDate").value(
                    Matchers.startsWith(
                        member.createDate.toString().take(20)
                    )
                )
            )
            .andExpect(
                jsonPath("$.data.item.modifyDate").value(
                    Matchers.startsWith(
                        member.modifyDate.toString().take(20)
                    )
                )
            )
            .andExpect(jsonPath("$.data.item.name").value(member.name))
            .andExpect(jsonPath("$.data.item.isAdmin").value(member.isAdmin))
            .andExpect(jsonPath("$.data.apiKey").value(member.apiKey))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty)

        resultActions.andExpect { result ->
            val apiKeyCookie = result.response.getCookie("apiKey").getOrThrow()
            assertThat(apiKeyCookie.value).isEqualTo(member.apiKey)
            assertThat(apiKeyCookie.path).isEqualTo("/")
            assertThat(apiKeyCookie.getAttribute("HttpOnly")).isEqualTo("true")

            val accessTokenCookie = result.response.getCookie("accessToken").getOrThrow()
            assertThat(accessTokenCookie.value).isNotBlank
            assertThat(accessTokenCookie.path).isEqualTo("/")
            assertThat(accessTokenCookie.getAttribute("HttpOnly")).isEqualTo("true")
        }
    }

    @Test
    @DisplayName("내 정보")
    @WithUserDetails("user1")
    fun t3() {
        val resultActions = mvc
            .perform(
                get("/api/v1/members/me")
            )
            .andDo(print())

        val member = memberService.findByUsername("user1").getOrThrow()

        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(member.id))
            .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(member.createDate.toString().take(20))))
            .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(member.modifyDate.toString().take(20))))
            .andExpect(jsonPath("$.name").value(member.name))
            .andExpect(jsonPath("$.username").value(member.username))
            .andExpect(jsonPath("$.isAdmin").value(member.isAdmin))
    }

    @Test
    @DisplayName("내 정보, with apiKey Cookie")
    fun t4() {
        val actor = memberService.findByUsername("user1").getOrThrow()
        val actorApiKey = actor.apiKey

        val resultActions = mvc
            .perform(
                get("/api/v1/members/me")
                    .cookie(Cookie("apiKey", actorApiKey))
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("me"))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("로그아웃")
    fun t6() {
        val resultActions = mvc
            .perform(
                delete("/api/v1/members/logout")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("logout"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("로그아웃 되었습니다."))
            .andExpect { result ->
                val apiKeyCookie = result.response.getCookie("apiKey").getOrThrow()
                assertThat(apiKeyCookie.value).isEmpty()
                assertThat(apiKeyCookie.maxAge).isEqualTo(0)
                assertThat(apiKeyCookie.path).isEqualTo("/")
                assertThat(apiKeyCookie.isHttpOnly).isTrue

                val accessTokenCookie = result.response.getCookie("accessToken").getOrThrow()
                assertThat(accessTokenCookie.value).isEmpty()
                assertThat(accessTokenCookie.maxAge).isEqualTo(0)
                assertThat(accessTokenCookie.path).isEqualTo("/")
                assertThat(accessTokenCookie.isHttpOnly).isTrue
            }
    }

    @Test
    @DisplayName("엑세스 토큰이 만료되었거나 유효하지 않다면 apiKey를 통해서 재발급")
    fun t7() {
        val actor = memberService.findByUsername("user1").getOrThrow()
        val actorApiKey = actor.apiKey

        val resultActions = mvc
            .perform(
                get("/api/v1/members/me")
                    .header("Authorization", "Bearer $actorApiKey wrong-access-token")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("me"))
            .andExpect(status().isOk)

        resultActions.andExpect { result ->
            val accessTokenCookie = result.response.getCookie("accessToken").getOrThrow()
            assertThat(accessTokenCookie.value).isNotBlank
            assertThat(accessTokenCookie.path).isEqualTo("/")
            assertThat(accessTokenCookie.getAttribute("HttpOnly")).isEqualTo("true")

            val headerAuthorization = result.response.getHeader("Authorization")
            assertThat(headerAuthorization).isNotBlank

            assertThat(headerAuthorization).isEqualTo(accessTokenCookie.value)
        }
    }

    @Test
    @DisplayName("Authorization 헤더가 Bearer 형식이 아닐 때 오류")
    fun t8() {
        val resultActions = mvc
            .perform(
                get("/api/v1/members/me")
                    .header("Authorization", "key")
            )
            .andDo(print())

        resultActions
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.resultCode").value("401-2"))
            .andExpect(jsonPath("$.msg").value("Authorization 헤더가 Bearer 형식이 아닙니다."))
    }
}
