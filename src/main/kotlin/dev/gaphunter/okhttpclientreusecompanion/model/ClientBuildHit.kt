package dev.gaphunter.okhttpclientreusecompanion.model

import com.intellij.psi.PsiElement

/** One `OkHttpClient` construction (`new OkHttpClient()` or `new OkHttpClient.Builder().build()`) found inside a non-constructor method body. */
data class ClientBuildHit(val callElement: PsiElement)
