package com.back.domain.post.postComment.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.post.post.entity.Post
import com.back.global.exception.ServiceException
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne

@Entity
class PostComment(
    @field:ManyToOne val author: Member,
    @field:ManyToOne val post: Post,
    var content: String,
) : BaseEntity() {
    fun modify(content: String) {
        this.content = content
    }

    fun checkActorCanModify(actor: Member) {
        if (author != actor) throw ServiceException("403-1", "${id}번 댓글 수정권한이 없습니다.")
    }

    fun checkActorCanDelete(actor: Member) {
        if (author != actor) throw ServiceException("403-2", "${id}번 댓글 삭제권한이 없습니다.")
    }
}