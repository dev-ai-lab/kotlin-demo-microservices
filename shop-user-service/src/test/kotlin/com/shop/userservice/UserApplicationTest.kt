package com.shop.userservice

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class UserApplicationTest {
    @Test
    fun testRoot() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load())
        }

        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Welcome to shop!", response.bodyAsText())
    }
}