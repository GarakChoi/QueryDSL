package com.back.global.initData

import com.back.standard.util.Ut.cmd.runAsync
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("dev")
@Configuration
class DevInitData {
    @Bean
    fun devInitDataApplicationRunner(): ApplicationRunner {
        return ApplicationRunner { args: ApplicationArguments ->
            runAsync(
                "npx{{DOT_CMD}}",
                "--yes",
                "--package", "typescript",
                "--package", "openapi-typescript",
                "openapi-typescript", "http://localhost:8080/v3/api-docs/apiV1",
                "-o", "../frontend/src/global/backend/apiV1/schema.d.ts",
                "--properties-required-by-default"
            )
        }
    }
}