package com.back.standard.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*

object Ut {
    object jwt {
        fun toString(secret: String, expireSeconds: Int, body: Map<String, Any>): String {
            val issuedAt = Date()
            val expiration = Date(issuedAt.time + 1000L * expireSeconds)

            val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

            val jwt = Jwts.builder()
                .claims(body)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact()

            return jwt
        }

        fun isValid(secret: String, jwtStr: String): Boolean {
            return try {
                val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr)

                true
            } catch (e: Exception) {
                false
            }
        }

        fun payload(secret: String, jwtStr: String): Map<String, Any>? {
            return try {
                val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr)
                    .payload as Map<String, Any>

            } catch (e: Exception) {
                null
            }
        }
    }

    object json {
        lateinit var objectMapper: ObjectMapper

        fun toString(obj: Any, defaultValue: String = ""): String {
            return try {
                objectMapper.writeValueAsString(obj)
            } catch (e: Exception) {
                defaultValue
            }
        }
    }

    object cmd {
        fun run(vararg args: String) {
            val isWindows = System
                .getProperty("os.name")
                .lowercase(Locale.getDefault())
                .contains("win")

            val builder = ProcessBuilder(
                args
                    .map { it.replace("{{DOT_CMD}}", if (isWindows) ".cmd" else "") }
                    .toList()
            )

            // 에러 스트림도 출력 스트림과 함께 병합
            builder.redirectErrorStream(true)

            // 프로세스 시작
            val process = builder.start()

            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { println(it) }
            }

            val exitCode = process.waitFor()

            println("종료 코드: $exitCode")
        }

        fun runAsync(vararg args: String) {
            Thread(Runnable {
                run(*args)
            }).start()
        }
    }
}
