package dev.gaphunter.okhttpclientreusecompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KotlinClientBuildFinderTest : BasePlatformTestCase() {

    fun `test a client built via direct construct inside a regular function is flagged`() {
        val file = myFixture.configureByText(
            "ApiService.kt",
            """
            class ApiService {
                fun call() {
                    val client = OkHttpClient()
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinClientBuildFinder.findAll(file).size)
    }

    fun `test a client built via builder inside a regular function is flagged`() {
        val file = myFixture.configureByText(
            "ApiService.kt",
            """
            class ApiService {
                fun call() {
                    val client = OkHttpClient.Builder().build()
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinClientBuildFinder.findAll(file).size)
    }

    fun `test a client built as a class property is not flagged`() {
        val file = myFixture.configureByText(
            "ApiService.kt",
            """
            class ApiService {
                val client = OkHttpClient()
            }
            """.trimIndent(),
        )
        assertTrue(KotlinClientBuildFinder.findAll(file).isEmpty())
    }

    fun `test an unrelated call expression is never flagged`() {
        val file = myFixture.configureByText(
            "ApiService.kt",
            """
            class ApiService {
                fun call() {
                    val sb = StringBuilder()
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinClientBuildFinder.findAll(file).isEmpty())
    }
}
