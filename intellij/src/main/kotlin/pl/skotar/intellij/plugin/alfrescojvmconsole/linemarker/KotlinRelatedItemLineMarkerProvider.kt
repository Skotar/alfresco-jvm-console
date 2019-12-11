package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.kotlin.psi.psiUtil.parents
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.getActiveFileOrNull
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.isFileInAnyModule

internal class KotlinRelatedItemLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val project = element.project
        val activeFile = project.getActiveFileOrNull() ?: return null

        if (isKtNamedFunction(element)) {
            val ktNamedFunction = (element as KtNamedFunction)

            if (
                project.isFileInAnyModule(activeFile) &&
                startsWithAlfresco(ktNamedFunction) &&
                isInClass(ktNamedFunction) &&
                isPublic(ktNamedFunction) &&
                hasNoParameters(ktNamedFunction)
            ) {
                return KotlinJvmConsoleLineMarkerInfo(ktNamedFunction)
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

    private fun isPublic(ktNamedFunction: KtNamedFunction): Boolean =
        ktNamedFunction.isPublic

    private fun hasNoParameters(function: KtNamedFunction): Boolean =
        function.valueParameterList?.parameters?.size == 0
}