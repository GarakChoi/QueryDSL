package com.back.global.aspect

import com.back.global.rsData.RsData
import jakarta.servlet.http.HttpServletResponse
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class ResponseAspect(
    private val response: HttpServletResponse, // 스프링이 요청 스코프 프록시로 주입
) {
    @Around(
        """
            execution(public com.back.global.rsData.RsData *(..)) &&
            (
                within(@org.springframework.stereotype.Controller *) ||
                within(@org.springframework.web.bind.annotation.RestController *)
            ) &&
            (
                @annotation(org.springframework.web.bind.annotation.GetMapping) ||
                @annotation(org.springframework.web.bind.annotation.PostMapping) ||
                @annotation(org.springframework.web.bind.annotation.PutMapping) ||
                @annotation(org.springframework.web.bind.annotation.DeleteMapping) ||
                @annotation(org.springframework.web.bind.annotation.RequestMapping)
            )
        """
    )
    fun handleResponseStrict(pjp: ProceedingJoinPoint): RsData<*> {
        val result = pjp.proceed() as RsData<*>

        response.status = result.statusCode

        return result
    }
}