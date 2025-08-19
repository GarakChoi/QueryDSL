package com.back.domain.post.postComment.controller

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.post.service.PostService
import com.back.standard.extensions.getOrThrow
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
class ApiV1PostCommentControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var postService: PostService

    @Autowired
    private lateinit var memberService: MemberService

    @Test
    @DisplayName("댓글 단건조회")
    fun t1() {
        val postId = 1
        val id = 1

        val resultActions = mvc
            .perform(
                get("/api/v1/posts/$postId/comments/$id")
            )
            .andDo(print())

        val post = postService.findById(postId).getOrThrow()
        val postComment = post.findCommentById(id).getOrThrow()

        resultActions
            .andExpect(handler().handlerType(ApiV1PostCommentController::class.java))
            .andExpect(handler().methodName("getItem"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(postComment.id))
            .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(postComment.createDate.toString().take(20))))
            .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(postComment.modifyDate.toString().take(20))))
            .andExpect(jsonPath("$.authorId").value(postComment.author.id))
            .andExpect(jsonPath("$.authorName").value(postComment.author.name))
            .andExpect(jsonPath("$.postId").value(postComment.post.id))
            .andExpect(jsonPath("$.content").value(postComment.content))
    }

    @Test
    @DisplayName("댓글 다건조회")
    fun t2() {
        val postId = 1

        val resultActions = mvc
            .perform(
                get("/api/v1/posts/$postId/comments")
            )
            .andDo(print())

        val post = postService.findById(postId).getOrThrow()
        val comments = post.comments

        resultActions
            .andExpect(handler().handlerType(ApiV1PostCommentController::class.java))
            .andExpect(handler().methodName("getItems"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(comments.size))

        for (i in comments.indices) {
            val postComment = comments[i]

            resultActions
                .andExpect(jsonPath("$[$i].id").value(postComment.id))
                .andExpect(
                    jsonPath("$[$i].createDate").value(
                        Matchers.startsWith(
                            postComment.createDate.toString().take(20)
                        )
                    )
                )
                .andExpect(
                    jsonPath("$[$i].modifyDate").value(
                        Matchers.startsWith(
                            postComment.modifyDate.toString().take(20)
                        )
                    )
                )
                .andExpect(jsonPath("$[$i].authorId").value(postComment.author.id))
                .andExpect(jsonPath("$[$i].authorName").value(postComment.author.name))
                .andExpect(jsonPath("$[$i].postId").value(postComment.post.id))
                .andExpect(jsonPath("$[$i].content").value(postComment.content))
        }
    }

    @Test
    @DisplayName("댓글 삭제")
    @WithUserDetails("user1")
    fun t3() {
        val postId = 1
        val id = 1

        val resultActions = mvc
            .perform(
                delete("/api/v1/posts/$postId/comments/$id")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostCommentController::class.java))
            .andExpect(handler().methodName("delete"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${id}번 댓글이 삭제되었습니다."))
    }

    @Test
    @DisplayName("댓글 삭제, without permission")
    fun t7() {
        val postId = 1
        val id = 1

        val actor = memberService.findByUsername("user3").getOrThrow()
        val actorApiKey = actor.apiKey

        val resultActions = mvc
            .perform(
                delete("/api/v1/posts/$postId/comments/$id")
                    .header("Authorization", "Bearer $actorApiKey")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostCommentController::class.java))
            .andExpect(handler().methodName("delete"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-2"))
            .andExpect(jsonPath("$.msg").value("${id}번 댓글 삭제권한이 없습니다."))
    }

    @Test
    @DisplayName("댓글 수정")
    @WithUserDetails("user1")
    fun t4() {
        val postId = 1
        val id = 1

        val resultActions = mvc
            .perform(
                put("/api/v1/posts/$postId/comments/$id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "content": "내용 new"
                        }
                    """
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostCommentController::class.java))
            .andExpect(handler().methodName("modify"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${id}번 댓글이 수정되었습니다."))
    }

    @Test
    @DisplayName("댓글 수정, without permission")
    fun t6() {
        val postId = 1
        val id = 1

        val actor = memberService.findByUsername("user3").getOrThrow()
        val actorApiKey = actor.apiKey

        val resultActions = mvc
            .perform(
                put("/api/v1/posts/$postId/comments/$id")
                    .header("Authorization", "Bearer $actorApiKey")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "content": "내용 new"
                        }
                    """
                    )
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1PostCommentController::class.java))
            .andExpect(handler().methodName("modify"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("${id}번 댓글 수정권한이 없습니다."))
    }

    @Test
    @DisplayName("댓글 작성")
    @WithUserDetails("user1")
    fun t5() {
        val postId = 1

        val resultActions = mvc
            .perform(
                post("/api/v1/posts/$postId/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "content": "내용"
                        }
                    """
                    )
            )
            .andDo(print())

        val post = postService.findById(postId).getOrThrow()
        val postComment = post.comments.last()

        resultActions
            .andExpect(handler().handlerType(ApiV1PostCommentController::class.java))
            .andExpect(handler().methodName("write"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.resultCode").value("201-1"))
            .andExpect(jsonPath("$.msg").value("${postComment.id}번 댓글이 작성되었습니다."))
            .andExpect(jsonPath("$.data.id").value(postComment.id))
            .andExpect(
                jsonPath("$.data.createDate").value(
                    Matchers.startsWith(
                        postComment.createDate.toString().take(20)
                    )
                )
            )
            .andExpect(
                jsonPath("$.data.modifyDate").value(
                    Matchers.startsWith(
                        postComment.modifyDate.toString().take(20)
                    )
                )
            )
            .andExpect(jsonPath("$.data.authorId").value(postComment.author.id))
            .andExpect(jsonPath("$.data.authorName").value(postComment.author.name))
            .andExpect(jsonPath("$.data.postId").value(postComment.post.id))
            .andExpect(jsonPath("$.data.content").value("내용"))
    }
}