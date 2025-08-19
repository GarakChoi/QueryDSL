package com.back.domain.post.post.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.post.postComment.entity.PostComment
import com.back.global.exception.ServiceException
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.CascadeType.PERSIST
import jakarta.persistence.CascadeType.REMOVE
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class Post(
    @field:ManyToOne val author: Member,
    var title: String,
    var content: String
) : BaseEntity() {
    @OneToMany(
        mappedBy = "post",
        cascade = [PERSIST, REMOVE],
        orphanRemoval = true
    )
    val comments: MutableList<PostComment> = mutableListOf()

    fun modify(title: String, content: String) {
        this.title = title
        this.content = content
    }

    fun addComment(author: Member, content: String): PostComment {
        val postComment = PostComment(author, this, content)
        comments.add(postComment)

        return postComment
    }

    fun findCommentById(id: Int): PostComment? {
        return comments.find { it.id == id }
    }

    fun deleteComment(postComment: PostComment): Boolean {
        return comments.remove(postComment)
    }

    fun checkActorCanModify(actor: Member) {
        if (author != actor) throw ServiceException("403-1", "${id}번 글 수정권한이 없습니다.")
    }

    fun checkActorCanDelete(actor: Member) {
        if (author != actor) throw ServiceException("403-2", "${id}번 글 삭제권한이 없습니다.")
    }
}