package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.PsiJavaFileImpl
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.ClassDescriptor

internal class JavaJvmConsoleLineMarkerInfo(
    element: PsiMethod
) : LineMarkerInfo<PsiMethod>(
    element,
    element.textRange,
    AllIcons.RunConfigurations.TestState.Run,
    null,
    null,
    GutterIconRenderer.Alignment.LEFT
) {

    override fun createGutterRenderer(): GutterIconRenderer =
        object : LineMarkerGutterIconRenderer<PsiMethod>(this) {

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

            private fun getPackageName(method: PsiMethod): String =
                (method.containingFile as PsiJavaFileImpl).packageName

            private fun getClassName(method: PsiMethod): String =
                method.containingClass!!.name!!

            private fun getFunctionName(method: PsiMethod): String =
                method.name

            private fun getMethodComments(method: PsiMethod): List<String> =
                method
                    .children.filterIsInstance<PsiCodeBlock>().first()
                    .children.filterIsInstance<PsiComment>().map(PsiElement::getText)

            override fun isNavigateAction(): Boolean =
                true

            override fun getPopupMenuActions(): ActionGroup? =
                null
        }
}