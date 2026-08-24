package dev.gaphunter.okhttpclientreusecompanion.detect

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import dev.gaphunter.okhttpclientreusecompanion.model.ClientBuildHit
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/** Kotlin counterpart of [JavaClientBuildFinder]. */
object KotlinClientBuildFinder {

    fun findAll(file: PsiFile): List<ClientBuildHit> {
        if (file !is KtFile) return emptyList()
        val hits = mutableListOf<ClientBuildHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                super.visitCallExpression(expression)
                hitForDirectConstruct(expression)?.let { hits += it }
            }

            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                super.visitDotQualifiedExpression(expression)
                hitForBuilderBuild(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitForDirectConstruct(call: KtCallExpression): ClientBuildHit? {
        if (call.calleeExpression?.text != "OkHttpClient") return null
        return hitIfNotInConstructor(call)
    }

    private fun hitForBuilderBuild(buildExpr: KtDotQualifiedExpression): ClientBuildHit? {
        val buildCall = buildExpr.selectorExpression as? KtCallExpression ?: return null
        if (buildCall.calleeExpression?.text != "build") return null

        // Accept `OkHttpClient.Builder().build()` -- the direct chained-construction form.
        val builderExpr = buildExpr.receiverExpression as? KtDotQualifiedExpression ?: return null
        val builderCall = builderExpr.selectorExpression as? KtCallExpression ?: return null
        if (builderCall.calleeExpression?.text != "Builder") return null
        if (builderExpr.receiverExpression.text != "OkHttpClient") return null

        return hitIfNotInConstructor(buildExpr)
    }

    private fun hitIfNotInConstructor(element: PsiElement): ClientBuildHit? {
        if (PsiTreeUtil.getParentOfType(element, KtConstructor::class.java) != null) return null
        if (PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java) == null) return null
        return ClientBuildHit(leafOf(element))
    }

    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
