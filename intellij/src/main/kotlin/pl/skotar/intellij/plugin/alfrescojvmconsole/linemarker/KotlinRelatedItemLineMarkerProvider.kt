package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.kotlin.psi.psiUtil.parents
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.getActiveFile
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.isFileInAnyModule

class KotlinRelatedItemLineMarkerProvider : LineMarkerProvider, AbstractRelatedItemLineMarkerProvider() {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val project = element.project

        if (isKtNamedFunction(element)) {
            val ktNamedFunction = (element as KtNamedFunction)

            if (
                project.isFileInAnyModule(project.getActiveFile()) &&
                startsWithAlfresco(ktNamedFunction) &&
                isInClass(ktNamedFunction) &&
                ktNamedFunction.isPublic &&
                hasNoParameters(ktNamedFunction)
            ) {
                return AlfrescoJvmConsoleLineMarkerInfo(
                    element,
                    createOnClickHandler(
                        project,
                        { getClassQualifiedName(ktNamedFunction) },
                        { ktNamedFunction.name!! }
                    )
                )
            }
        }

        return null
    }

    override fun collectSlowLineMarkers(elements: MutableList<PsiElement>, result: MutableCollection<LineMarkerInfo<PsiElement>>) {
        // deliberately omitted
    }

    private fun isKtNamedFunction(element: PsiElement): Boolean =
        element is KtNamedFunction

    private fun startsWithAlfresco(function: KtNamedFunction): Boolean =
        function.nameAsSafeName.asString().startsWith("alfresco", true)

    private fun isInClass(function: KtNamedFunction): Boolean =
        function.parents.filterIsInstance<KtClass>().count() == 1 &&
                function.parents.filterIsInstance<KtObjectDeclaration>().count() == 0

    private fun hasNoParameters(function: KtNamedFunction): Boolean =
        function.valueParameterList?.parameters?.size == 0

    private fun getClassQualifiedName(function: KtNamedFunction): String =
        function.containingClass()!!.getKotlinFqName()!!.asString()
}