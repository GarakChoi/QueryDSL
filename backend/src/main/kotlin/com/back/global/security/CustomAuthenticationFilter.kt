package com.back.global.security

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import com.back.global.exception.ServiceException
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import com.back.standard.util.Ut
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class CustomAuthenticationFilter(
    private val memberService: MemberService,
    private val rq: Rq,
) : OncePerRequestFilter() {

    private val publicApiPaths = setOf(
        "/api/v1/members/login",
        "/api/v1/members/logout",
        "/api/v1/members/join",
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            work(request, response, filterChain)
        } catch (e: ServiceException) {
            val rsData: RsData<Void> = e.rsData
            response.contentType = "$APPLICATION_JSON_VALUE; charset=UTF-8"
            response.status = rsData.statusCode
            response.writer.write(Ut.json.toString(rsData))
        }
    }

    private fun work(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        if (!isApiRequest(request) || isPublicApi(request)) {
            filterChain.doFilter(request, response)
            return
        }

        val (apiKey, accessToken) = extractTokens()
        if (apiKey.isBlank() && accessToken.isBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        val (member, isAccessTokenValid) = resolveMember(apiKey, accessToken)

        if (accessToken.isNotBlank() && !isAccessTokenValid) {
            refreshAccessToken(member)
        }

        authenticate(member)

        filterChain.doFilter(request, response)
    }

    private fun isApiRequest(request: HttpServletRequest): Boolean =
        request.requestURI.startsWith("/api/")

    private fun isPublicApi(request: HttpServletRequest): Boolean =
        request.requestURI in publicApiPaths

    private fun extractTokens(): Pair<String, String> {
        val headerAuthorization = rq.getHeader("Authorization", "")

        return if (headerAuthorization.isNotBlank()) {
            if (!headerAuthorization.startsWith("Bearer "))
                throw ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.")
            val bits = headerAuthorization.split(" ", limit = 3)
            bits.getOrNull(1).orEmpty() to bits.getOrNull(2).orEmpty()
        } else {
            rq.getCookieValue("apiKey", "") to rq.getCookieValue("accessToken", "")
        }
    }

    private fun resolveMember(apiKey: String, accessToken: String): Pair<Member, Boolean> {
        memberFromAccessToken(accessToken)?.let { return it to true }

        val member = memberService.findByApiKey(apiKey)
            ?: throw ServiceException("401-3", "API 키가 유효하지 않습니다.")

        return member to false
    }

    private fun memberFromAccessToken(token: String): Member? {
        if (token.isBlank()) return null

        val payload = memberService.payload(token) ?: return null

        val id = payload["id"] as Int
        val username = payload["username"] as String
        val name = payload["name"] as String

        return Member(id, username, name)
    }

    private fun refreshAccessToken(member: Member) {
        val newToken = memberService.genAccessToken(member)

        rq.setCookie("accessToken", newToken)
        rq.setHeader("Authorization", newToken)
    }

    private fun authenticate(member: Member) {
        val user: UserDetails = SecurityUser(
            member.id,
            member.username,
            "",
            member.name,
            member.authorities
        )

        val authentication: Authentication =
            UsernamePasswordAuthenticationToken(user, user.password, user.authorities)

        SecurityContextHolder.getContext().authentication = authentication
    }
}