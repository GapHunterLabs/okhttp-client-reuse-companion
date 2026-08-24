package dev.gaphunter.okhttpclientreusecompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JavaClientBuildFinderTest : BasePlatformTestCase() {

    fun `test a client built via direct new inside a regular method is flagged`() {
        val file = myFixture.configureByText(
            "ApiService.java",
            """
            class ApiService {
                void call() {
                    OkHttpClient client = new OkHttpClient();
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaClientBuildFinder.findAll(file).size)
    }

    fun `test a client built via builder inside a regular method is flagged`() {
        val file = myFixture.configureByText(
            "ApiService.java",
            """
            class ApiService {
                void call() {
                    OkHttpClient client = new OkHttpClient.Builder().build();
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaClientBuildFinder.findAll(file).size)
    }

    fun `test a client built inside a constructor is not flagged`() {
        val file = myFixture.configureByText(
            "ApiService.java",
            """
            class ApiService {
                private final OkHttpClient client;
                ApiService() {
                    client = new OkHttpClient();
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaClientBuildFinder.findAll(file).isEmpty())
    }

    fun `test an unrelated new expression is never flagged`() {
        val file = myFixture.configureByText(
            "ApiService.java",
            """
            class ApiService {
                void call() {
                    StringBuilder sb = new StringBuilder();
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaClientBuildFinder.findAll(file).isEmpty())
    }
}
