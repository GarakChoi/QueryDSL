package com.back.domain.post.post.controller

import com.back.domain.post.post.service.PostService
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
class ApiV1AdmPostControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var postService: PostService


    @Test
    @DisplayName("count")
    @WithUserDetails("admin")
    fun t1() {
        val resultActions = mvc
            .perform(
                get("/api/v1/adm/posts/count")
            )
            .andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1AdmPostController::class.java))
            .andExpect(handler().methodName("count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.all").value(postService.count()))
    }
}
