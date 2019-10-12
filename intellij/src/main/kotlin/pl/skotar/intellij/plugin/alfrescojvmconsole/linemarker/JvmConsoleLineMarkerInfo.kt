package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement

internal class JvmConsoleLineMarkerInfo<T : PsiElement>(
    element: T,
    private val executeOnClick: () -> Unit
) : LineMarkerInfo<T>(
    element,
    element.textRange,
    AllIcons.RunConfigurations.TestState.Run,
    null,
    null,
    GutterIconRenderer.Alignment.LEFT
) {

    override fun createGutterRenderer(): GutterIconRenderer =
        object : LineMarkerGutterIconRenderer<T>(this) {

            override fun getClickAction(): AnAction? =
                object : AnAction("Execute on Alfresco", null, AllIcons.RunConfigurations.TestState.Run) {
                    override fun actionPerformed(e: AnActionEvent) {
                        executeOnClick()
                    }
                }

            override fun getTooltipText(): String? {
                return "Run 'Execute on Alfresco'"
            }

            override fun isNavigateAction(): Boolean =
                true

            override fun getPopupMenuActions(): ActionGroup? =
                null
        }
}