package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.psiUtil.parents
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.getActiveFileOrNull
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.isFileInAnyModule

internal class JavaRelatedItemLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val project = element.project
        val activeFile = project.getActiveFileOrNull() ?: return null

        if (isMethod(element)) {
            val psiMethod = (element.parent as PsiMethod)

            if (
                project.isFileInAnyModule(activeFile) &&
                startsWithAlfresco(psiMethod) &&
                isInClass(psiMethod) &&
                isPublicNotStatic(psiMethod) &&
                hasNoParameters(psiMethod)
            ) {
                return JavaJvmConsoleLineMarkerInfo(psiMethod)
            }
        }

        return null
    }

    override fun collectSlowLineMarkers(elements: MutableList<PsiElement>, result: MutableCollection<LineMarkerInfo<PsiElement>>) {
        // deliberately omitted
    }

    private fun isMethod(element: PsiElement): Boolean =
        element is PsiIdentifier && element.parent is PsiMethod

    private fun startsWithAlfresco(method: PsiMethod): Boolean =
        method.name.startsWith("alfresco", true)

    private fun isInClass(psiMethod: PsiMethod): Boolean =
        psiMethod.parents.filterIsInstance<PsiClass>().count() == 1

    private fun isPublicNotStatic(method: PsiMethod): Boolean =
        method.modifierList.hasExplicitModifier("public") && !method.modifierList.hasExplicitModifier("static")

    private fun hasNoParameters(method: PsiMethod): Boolean =
        method.parameterList.parametersCount == 0
}