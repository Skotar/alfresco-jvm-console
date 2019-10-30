package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.kotlin.psi.psiUtil.parents
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.ClassDescriptor
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.getActiveFile
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.isFileInAnyModule

internal class KotlinRelatedItemLineMarkerProvider : LineMarkerProvider, AbstractRelatedItemLineMarkerProvider() {

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
                val packageName = element.containingKtFile.packageFqName.asString()
                return AlfrescoJvmConsoleLineMarkerInfo(element, createOnClickHandler(project, { getMethodComments(ktNamedFunction) }) {
                    ClassDescriptor(getPackageName(ktNamedFunction, packageName), getClassName(ktNamedFunction), getFunctionName(ktNamedFunction))
                })
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

    private fun getPackageName(function: KtNamedFunction, fallbackPackageName: String): String =
        try {
            (function.containingFile as KtFile).packageFqName.asString()
        } catch (e: Exception) {
            fallbackPackageName
        }

    private fun getClassName(function: KtNamedFunction): String =
        function.containingClass()!!.name!!

    private fun getFunctionName(function: KtNamedFunction): String =
        function.name!!

    private fun getMethodComments(function: KtNamedFunction): List<String> =
        (function.bodyBlockExpression!! as ASTNode).children()
            .filterIsInstance<PsiComment>().map(PsiElement::getText)
            .toList()
}