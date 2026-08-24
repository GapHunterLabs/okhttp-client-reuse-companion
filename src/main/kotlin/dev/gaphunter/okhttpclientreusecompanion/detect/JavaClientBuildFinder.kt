package dev.gaphunter.okhttpclientreusecompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiNewExpression
import com.intellij.psi.util.PsiTreeUtil
import dev.gaphunter.okhttpclientreusecompanion.model.ClientBuildHit

/**
 * Finds `new OkHttpClient()` or `new OkHttpClient.Builder().build()`
 * constructions written inside a non-constructor method body --
 * OkHttp's own documentation states "OkHttp performs best when you
 * create a single OkHttpClient instance and reuse it for all of your
 * HTTP calls, because each client holds its own connection pool and
 * thread pools... creating a client for each request wastes resources
 * on idle pools". Building one inside a regular method means a brand
 * new connection pool (and thread pools) is created on every call.
 *
 * **v0.1 scope, stated honestly:** only the "build from scratch" shape
 * is flagged (`new OkHttpClient()` or `new OkHttpClient.Builder()
 * .build()`) -- `existingClient.newBuilder()....build()`, OkHttp's own
 * documented pattern for customizing a shared client without
 * duplicating its connection pool, is never flagged (correctly, since
 * it isn't the anti-pattern this plugin targets).
 */
object JavaClientBuildFinder {

    fun findAll(file: PsiFile): List<ClientBuildHit> {
        val hits = mutableListOf<ClientBuildHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitNewExpression(expression: PsiNewExpression) {
                super.visitNewExpression(expression)
                hitForDirectNew(expression)?.let { hits += it }
            }

            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                hitForBuilderBuild(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitForDirectNew(newExpr: PsiNewExpression): ClientBuildHit? {
        val className = newExpr.classReference?.referenceName ?: return null
        if (className != "OkHttpClient") return null
        return hitIfNotInConstructor(newExpr)
    }

    private fun hitForBuilderBuild(buildCall: PsiMethodCallExpression): ClientBuildHit? {
        if (buildCall.methodExpression.referenceName != "build") return null

        // Accept `new OkHttpClient.Builder().build()` (the direct chained-new form) -- a
        // builder assigned to an intermediate variable before `.build()` isn't traced in v0.1.
        val builderNew = buildCall.methodExpression.qualifierExpression as? PsiNewExpression ?: return null
        val classRef = builderNew.classReference ?: return null
        if (classRef.qualifiedName != "OkHttpClient.Builder") return null

        return hitIfNotInConstructor(buildCall)
    }

    private fun hitIfNotInConstructor(element: PsiElement): ClientBuildHit? {
        val containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return null
        if (containingMethod.isConstructor) return null
        return ClientBuildHit(leafOf(element))
    }

    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
