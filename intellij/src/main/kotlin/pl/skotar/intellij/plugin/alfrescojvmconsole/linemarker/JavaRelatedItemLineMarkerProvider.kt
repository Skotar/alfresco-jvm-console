package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiJavaFileImpl
import org.jetbrains.kotlin.psi.psiUtil.parents
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.ClassDescriptor
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.getActiveFile
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.isFileInAnyModule

internal class JavaRelatedItemLineMarkerProvider : LineMarkerProvider, AbstractRelatedItemLineMarkerProvider() {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val project = element.project
        val activeFile = project.getActiveFile()

        if (isMethod(element)) {
            val psiMethod = (element.parent as PsiMethod)

            if (
                project.isFileInAnyModule(activeFile) &&
                startsWithAlfresco(psiMethod) &&
                isInClass(psiMethod) &&
                isPublicNotStatic(psiMethod) &&
                hasNoParameters(psiMethod)
            ) {
                return AlfrescoJvmConsoleLineMarkerInfo(element, createOnClickHandler(project, { getMethodComments(psiMethod) }) {
                    ClassDescriptor(getPackageName(psiMethod), getClassName(psiMethod), getFunctionName(psiMethod))
                })
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
        psiMethod.parents
            .filterIsInstance<PsiClass>()
            .count() == 1

    private fun isPublicNotStatic(method: PsiMethod): Boolean =
        method.modifierList.hasExplicitModifier("public") && !method.modifierList.hasExplicitModifier("static")

    private fun hasNoParameters(method: PsiMethod): Boolean =
        method.parameters.isEmpty()

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
}