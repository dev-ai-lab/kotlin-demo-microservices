package com.shop.userservice

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import com.shop.userservice.config.Health
import com.shop.userservice.config.HealthStatus

class UserApplicationIntTest {
    @Test
    fun testRoot() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load())
        }

        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Welcome to shop!", response.bodyAsText())
    }

    @Test
    fun testHealth() = testApplication {
        environment {
            config = HoconApplicationConfig(ConfigFactory.load())
        }

        val healthResponse = client.get("/health")
        assertEquals(HttpStatusCode.OK, healthResponse.status)

        val body = healthResponse.bodyAsText()
        val health = Json.decodeFromString<Health>(body)
        assertEquals(HealthStatus.HEALTHY, health.health)
    }
}