package com.example

import com.example.models.ApiResponse
import com.example.repository.HeroRepositoryImpl
import com.example.repository.NEXT_PAGE_KEY
import com.example.repository.PREV_PAGE_KEY
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.test.*

class ApplicationTest {
    @Test
    fun `access root endpoint, assert correct information`() = testApplication {
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Welcome to Boruto API!", bodyAsText())
        }
    }
    @ExperimentalSerializationApi
    @Test
    fun `access all heroes endpoint, query all pages, assert correct information`() =
        testApplication {
            val heroRepository = HeroRepositoryImpl()
            val pages = 1..5
            val heroes = listOf(
                heroRepository.page1,
                heroRepository.page2,
                heroRepository.page3,
                heroRepository.page4,
                heroRepository.page5
            )
            pages.forEach { page ->
                val response = client.get("/boruto/heroes?page=$page")
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status
                )
                val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())
                val expected = ApiResponse(
                    success = true,
                    message = "ok",
                    prevPage = calculatePage(page = page)["prevPage"],
                    nextPage = calculatePage(page = page)["nextPage"],
                    heroes = heroes[page - 1],
                    lastUpdated = actual.lastUpdated
                )
                assertEquals(
                    expected = expected,
                    actual = actual
                )
            }
        }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query hero name, assert single hero result`() = testApplication {
        client.get("/boruto/heroes/search?name=sas").apply {
            assertEquals(expected = HttpStatusCode.OK, actual = status)
            val actual = Json.decodeFromString<ApiResponse>(body())
                .heroes.size
            assertEquals(expected = 1, actual = actual)
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query hero name, assert multiple heroes result`() = testApplication {
        client.get("/boruto/heroes/search?name=sa").apply {
            assertEquals(expected = HttpStatusCode.OK, actual = status)
            val actual = Json.decodeFromString<ApiResponse>(body())
                .heroes.size
            assertEquals(expected = 3, actual = actual)
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query an empty text, assert empty list as a result`() = testApplication {
        client.get("/boruto/heroes/search?name=").apply {
            assertEquals(expected = HttpStatusCode.OK, actual = status)
            val actual = Json.decodeFromString<ApiResponse>(body())
                .heroes
            assertEquals(expected = emptyList(), actual = actual)
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query non existing hero, assert empty list as a result`() = testApplication {
        client.get("/boruto/heroes/search?name=unknown").apply {
            assertEquals(expected = HttpStatusCode.OK, actual = status)
            val actual = Json.decodeFromString<ApiResponse>(body())
                .heroes
            assertEquals(expected = emptyList(), actual = actual)
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun `access non existing endpoint, assert not found`() = testApplication {
        client.get("/unknown").apply {
            assertEquals(expected = HttpStatusCode.NotFound, actual = status)
            assertEquals(expected = "404: Page Not Found", actual = body())
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun `access all heroes endpoint, query non existing page number, assert error`() =
        testApplication {
            val response = client.get("/boruto/heroes?page=6")
            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = response.status
            )
            assertEquals(
                expected = "404: Page Not Found",
                actual = response.body()
            )
        }

    @ExperimentalSerializationApi
    @Test
    fun `access all heroes endpoint, query invalid page number, assert error`() = testApplication {
        client.get("/boruto/heroes?page=invalid").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            val expected = ApiResponse(
                success = false,
                message = "Only Numbers Allowed.",
            )
            val actual = Json.decodeFromString<ApiResponse>(body())
            println("EXPECTED: $expected")
            println("ACTUAL: $actual")
            assertEquals(expected = expected, actual = actual)
        }
    }

    private fun calculatePage(page: Int): Map<String, Int?>{
        var prevPage: Int? = page
        var nextPage: Int? = page
        if(page in 1..4){
            nextPage = nextPage?.plus(1)
        }
        if(page in 2..5){
            prevPage = prevPage?.minus(1)
        }
        if(page == 1){
            prevPage  = null
        }
        if(page == 5){
            nextPage = null
        }
        return mapOf(PREV_PAGE_KEY to prevPage, NEXT_PAGE_KEY to nextPage)
    }

}
