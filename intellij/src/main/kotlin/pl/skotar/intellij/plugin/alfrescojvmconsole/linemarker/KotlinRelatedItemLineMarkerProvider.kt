package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.getActiveFile
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.isFileInAnyModule

class KotlinRelatedItemLineMarkerProvider : LineMarkerProvider, AbstractRelatedItemLineMarkerProvider() {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        return null

        val project = element.project

        if (isKtNamedFunction(element)) {
            val ktNamedFunction = (element as KtNamedFunction)

            if (
                project.isFileInAnyModule(project.getActiveFile()) &&
                startsWithPromena(ktNamedFunction) &&
                isNotInClass(ktNamedFunction) &&
                hasNoParameters(ktNamedFunction)
            ) {
                return AlfrescoJvmConsoleLineMarkerInfo(
                    element,
                    createOnClickHandler(
                        project,
                        { ktNamedFunction.getClassQualifiedName() },
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

    private fun startsWithPromena(function: KtNamedFunction): Boolean =
        function.nameAsSafeName.asString().startsWith("promena", true)

    private fun isNotInClass(function: KtNamedFunction): Boolean =
        function.containingClass() == null

    private fun hasNoParameters(function: KtNamedFunction): Boolean =
        function.valueParameterList?.parameters?.size == 0

    private fun KtNamedFunction.getClassQualifiedName(): String =
        containingKtFile.packageFqName.asString() + "." + containingKtFile.name.removeSuffix(".kt") + "Kt"

    private fun getMethodComments(function: KtNamedFunction): List<String> =
        function.bodyBlockExpression!!.children()
            .filterIsInstance<PsiComment>().map { it.text }
            .toList()
}