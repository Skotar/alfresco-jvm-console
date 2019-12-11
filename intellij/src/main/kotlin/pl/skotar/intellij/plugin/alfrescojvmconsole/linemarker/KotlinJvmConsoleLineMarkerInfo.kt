package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.ClassDescriptor

internal class KotlinJvmConsoleLineMarkerInfo(
    element: KtNamedFunction
) : LineMarkerInfo<KtNamedFunction>(
    element,
    element.textRange,
    AllIcons.RunConfigurations.TestState.Run,
    null,
    null,
    GutterIconRenderer.Alignment.LEFT
) {

    override fun createGutterRenderer(): GutterIconRenderer =
        object : LineMarkerGutterIconRenderer<KtNamedFunction>(this) {

            override fun getTooltipText(): String? {
                return "Run 'Execute on Alfresco'"
            }

            override fun getClickAction(): AnAction? =
                object : AnAction("Execute on Alfresco", null, AllIcons.RunConfigurations.TestState.Run) {
                    override fun actionPerformed(e: AnActionEvent) {
                        val ktNamedFunction = element!!
                        CodeExecutor.execute(
                            ktNamedFunction.project,
                            ClassDescriptor(getPackageName(ktNamedFunction), getClassName(ktNamedFunction), getFunctionName(ktNamedFunction)),
                            getMethodComments(ktNamedFunction)
                        )
                    }
                }

            private fun getPackageName(function: KtNamedFunction): String =
                (function.containingFile as KtFile).packageFqName.asString()

            private fun getClassName(function: KtNamedFunction): String =
                function.containingClass()!!.name!!

            private fun getFunctionName(function: KtNamedFunction): String =
                function.name!!

            private fun getMethodComments(function: KtNamedFunction): List<String> =
                try {
                    (function.bodyBlockExpression!! as ASTNode).children()
                        .filterIsInstance<PsiComment>().map(PsiElement::getText)
                        .toList()
                } catch (e: KotlinNullPointerException) {
                    emptyList()
                }

            override fun isNavigateAction(): Boolean =
                true

            override fun getPopupMenuActions(): ActionGroup? =
                null
        }
}