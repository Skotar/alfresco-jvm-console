package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.psiUtil.parents
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.getActiveFile
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.isFileInAnyModule

class JavaRelatedItemLineMarkerProvider : LineMarkerProvider, AbstractRelatedItemLineMarkerProvider() {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val project = element.project
        val activeFile = project.getActiveFile()

        if (isMethod(element)) {
            val psiMethod = (element.parent as PsiMethod)

            if (
                project.isFileInAnyModule(activeFile) &&
                startsWithAlfresco(psiMethod) &&
                isNotInInnerClass(psiMethod) &&
                isPublicNotStatic(psiMethod) &&
                hasNoParameters(psiMethod)
            ) {
                return AlfrescoJvmConsoleLineMarkerInfo(
                    element,
                    createOnClickHandler(
                        project,
                        { psiMethod.getClassQualifiedName() },
                        { psiMethod.name }
                    )
                )
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

    private fun isNotInInnerClass(psiMethod: PsiMethod): Boolean =
        psiMethod.parents
            .filterIsInstance<PsiClass>()
            .count() == 1

    private fun isPublicNotStatic(method: PsiMethod): Boolean =
        method.modifierList.hasExplicitModifier("public") && !method.modifierList.hasExplicitModifier("static")

    private fun hasNoParameters(method: PsiMethod): Boolean =
        method.parameters.isEmpty()

    private fun PsiMethod.getClassQualifiedName(): String =
        containingClass!!.qualifiedName!!
}