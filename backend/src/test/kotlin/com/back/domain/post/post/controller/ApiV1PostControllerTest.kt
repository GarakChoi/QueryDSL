package com.back.domain.post.post.controller

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.post.service.PostService
import com.back.standard.extensions.getOrThrow
import jakarta.servlet.http.Cookie
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
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1PostControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var postService: PostService

    @Autowired
    private lateinit var memberService: MemberService

    @Test
    @DisplayName("글 쓰기")
    @WithUserDetails("user1")
    fun t1() {
        val resultActions = mvc
            .perform(
                post("/api/v1/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {
                                "title": "제목",
                                "content": "내용"
                            }
                            """
                    )
            )
            .andDo(print())

        val post = postService.findLatest().getOrThrow()

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("write"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.resultCode").value("201-1"))
            .andExpect(jsonPath("$.msg").value("%d번 글이 작성되었습니다.".format(post.id)))
            .andExpect(jsonPath("$.data.id").value(post.id))
            .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(post.createDate.toString().take(20))))
            .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(post.modifyDate.toString().take(20))))
            .andExpect(jsonPath("$.data.authorId").value(post.author.id))
            .andExpect(jsonPath("$.data.authorName").value(post.author.nickname))
            .andExpect(jsonPath("$.data.title").value("제목"))
    }

    @Test
    @DisplayName("글 쓰기, with wrong apiKey, with valid accessToken")
    fun t14() {
        val actor = memberService.findByUsername("user1").getOrThrow()
        val actorAccessToken = memberService.genAccessToken(actor)

        val resultActions = mvc
            .perform(
                post("/api/v1/posts")
                    .header("Authorization", "Bearer wrong-api-key $actorAccessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {
                                "title": "제목",
                                "content": "내용"
                            }
                            """
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("write"))
            .andExpect(status().isCreated)
    }

    @Test
    @DisplayName("글 쓰기, with wrong apiKey cookie, with valid accessToken cookie")
    fun t15() {
        val actor = memberService.findByUsername("user1").getOrThrow()
        val actorAccessToken = memberService.genAccessToken(actor)

        val resultActions = mvc
            .perform(
                post("/api/v1/posts")
                    .cookie(
                        Cookie("apiKey", "wrong-api-key"),
                        Cookie("accessToken", actorAccessToken)
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {
                                "title": "제목",
                                "content": "내용"
                            }
                            """
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("write"))
            .andExpect(status().isCreated)
    }

    @Test
    @DisplayName("글 쓰기, without title")
    @WithUserDetails("user1")
    fun t7() {
        val resultActions = mvc
            .perform(
                post("/api/v1/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {
                                "title": "",
                                "content": "내용"
                            }
                            """
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("write"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.resultCode").value("400-1"))
            .andExpect(
                jsonPath("$.msg").value(
                    """
                        title-NotBlank-must not be blank
                        title-Size-size must be between 2 and 100
                        """.trimIndent()
                )
            )
    }

    @Test
    @DisplayName("글 쓰기, without content")
    @WithUserDetails("user1")
    fun t8() {
        val resultActions = mvc
            .perform(
                post("/api/v1/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {
                                "title": "제목",
                                "content": ""
                            }
                            """
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("write"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.resultCode").value("400-1"))
            .andExpect(
                jsonPath("$.msg").value(
                    """
                        content-NotBlank-must not be blank
                        content-Size-size must be between 2 and 5000
                        """.trimIndent()
                )
            )
    }

    @Test
    @DisplayName("글 쓰기, with wrong json syntax")
    @WithUserDetails("user1")
    fun t9() {
        val wrongJsonBody = """
            {
                "title": 제목",
                content": "내용"
                
            """.trimIndent()

        val resultActions = mvc
            .perform(
                post("/api/v1/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(wrongJsonBody)
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("write"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.resultCode").value("400-1"))
            .andExpect(
                jsonPath("$.msg").value("요청 본문이 올바르지 않습니다.".trim())
            )
    }

    @Test
    @DisplayName("글 쓰기, without authorization header")
    fun t10() {
        val resultActions = mvc
            .perform(
                post("/api/v1/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {
                                "title": "제목",
                                "content": "내용"
                            }
                            """
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.resultCode").value("401-1"))
            .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."))
    }

    @Test
    @DisplayName("글 쓰기, with wrong authorization header")
    fun t11() {
        val resultActions = mvc
            .perform(
                post("/api/v1/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer wrong-api-key")
                    .content(
                        """
                            {
                                "title": "제목",
                                "content": "내용"
                            }
                            """
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.resultCode").value("401-3"))
            .andExpect(jsonPath("$.msg").value("API 키가 유효하지 않습니다."))
    }


    @Test
    @DisplayName("글 수정")
    @WithUserDetails("user1")
    fun t2() {
        val id = 1

        val resultActions = mvc
            .perform(
                put("/api/v1/posts/$id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {
                                "title": "제목 new",
                                "content": "내용 new"
                            }
                            """
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("modify"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("%d번 글이 수정되었습니다.".format(id)))
    }

    @Test
    @DisplayName("글 수정, without permission")
    fun t12() {
        val id = 1

        val actor = memberService.findByUsername("user3").getOrThrow()
        val actorApiKey = actor.apiKey

        val resultActions = mvc
            .perform(
                put("/api/v1/posts/$id")
                    .header("Authorization", "Bearer $actorApiKey")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {
                                "title": "제목 new",
                                "content": "내용 new"
                            }
                            """
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("modify"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("%d번 글 수정권한이 없습니다.".format(id)))
    }


    @Test
    @DisplayName("글 삭제")
    @WithUserDetails("user1")
    fun t3() {
        val id = 1

        val resultActions = mvc
            .perform(
                delete("/api/v1/posts/$id")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("delete"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("%d번 글이 삭제되었습니다.".format(id)))
    }

    @Test
    @DisplayName("글 삭제, without permission")
    fun t13() {
        val id = 1

        val actor = memberService.findByUsername("user3").getOrThrow()
        val actorApiKey = actor.apiKey

        val resultActions = mvc
            .perform(
                delete("/api/v1/posts/$id")
                    .header("Authorization", "Bearer $actorApiKey")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("delete"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-2"))
            .andExpect(jsonPath("$.msg").value("%d번 글 삭제권한이 없습니다.".format(id)))
    }


    @Test
    @DisplayName("글 단건조회")
    fun t4() {
        val id = 1

        val resultActions = mvc
            .perform(
                get("/api/v1/posts/$id")
            )
            .andDo(print())

        val post = postService.findById(id).getOrThrow()

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("getItem"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(post.id))
            .andExpect(
                jsonPath("$.createDate")
                    .value(Matchers.startsWith(post.createDate.toString().take(20)))
            )
            .andExpect(
                jsonPath("$.modifyDate")
                    .value(Matchers.startsWith(post.modifyDate.toString().take(20)))
            )
            .andExpect(jsonPath("$.authorId").value(post.author.id))
            .andExpect(jsonPath("$.authorName").value(post.author.nickname))
            .andExpect(jsonPath("$.title").value(post.title))
            .andExpect(jsonPath("$.content").value(post.content))
    }

    @Test
    @DisplayName("글 단건조회, 404")
    fun t6() {
        val id = Int.MAX_VALUE

        val resultActions = mvc
            .perform(
                get("/api/v1/posts/$id")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("getItem"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.resultCode").value("404-1"))
            .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."))
    }


    @Test
    @DisplayName("글 다건조회")
    fun t5() {
        val resultActions = mvc
            .perform(
                get("/api/v1/posts")
            )
            .andDo(print())

        val posts = postService.findAll()

        resultActions
            .andExpect(handler().handlerType(ApiV1PostController::class.java))
            .andExpect(handler().methodName("getItems"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(posts.size))

        for (i in posts.indices) {
            val post = posts[i]
            resultActions
                .andExpect(jsonPath("$[%d].id".format(i)).value(post.id))
                .andExpect(
                    jsonPath("$[%d].createDate".format(i))
                        .value(Matchers.startsWith(post.createDate.toString().take(20)))
                )
                .andExpect(
                    jsonPath("$[%d].modifyDate".format(i))
                        .value(Matchers.startsWith(post.modifyDate.toString().take(20)))
                )
                .andExpect(jsonPath("$[%d].authorId".format(i)).value(post.author.id))
                .andExpect(jsonPath("$[%d].authorName".format(i)).value(post.author.nickname))
                .andExpect(jsonPath("$[%d].title".format(i)).value(post.title))
        }
    }
}