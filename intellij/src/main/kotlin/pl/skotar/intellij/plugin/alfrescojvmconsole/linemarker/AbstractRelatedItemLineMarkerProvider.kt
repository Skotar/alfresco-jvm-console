package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.execution.RunManager
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.execution.impl.RunDialog
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleRunConfiguration
import pl.skotar.intellij.plugin.alfrescojvmconsole.executor.HttpExecutor
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.createAlfrescoJvmConsoleRunnerAndConfigurationSettings
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.getSelectedAlfrescoJvmConsoleRunnerAndConfigurationSettings
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.toFullString
import pl.skotar.intellij.plugin.alfrescojvmconsole.toolwindow.RunToolWindowTab
import pl.skotar.intellij.plugin.alfrescojvmconsole.toolwindow.logFailureThrowable
import pl.skotar.intellij.plugin.alfrescojvmconsole.toolwindow.logStart
import pl.skotar.intellij.plugin.alfrescojvmconsole.toolwindow.logSuccess
import pl.skotar.intellij.plugin.alfrescojvmconsole.util.invokeLater
import java.lang.System.currentTimeMillis
import java.util.concurrent.Executors

abstract class AbstractRelatedItemLineMarkerProvider {

    protected fun createOnClickHandler(
        project: Project,
        getQualifiedClassName: () -> String,
        getMethodName: () -> String
    ): () -> Unit =
        {
            val runManager = RunManager.getInstance(project)
            val runnerAndConfigurationSettings =
                (runManager.getSelectedAlfrescoJvmConsoleRunnerAndConfigurationSettings()
                    ?: runManager.createAlfrescoJvmConsoleRunnerAndConfigurationSettings())
                    .also { runManager.selectedConfiguration = it }

            try {
                runnerAndConfigurationSettings.checkSettings()

                val qualifiedClassName = getQualifiedClassName()
                val methodName = getMethodName()

                val alfrescoJvmConsoleRunConfiguration = runnerAndConfigurationSettings.configuration as AlfrescoJvmConsoleRunConfiguration
                val httpAddress =
                    alfrescoJvmConsoleRunConfiguration.host + alfrescoJvmConsoleRunConfiguration.path + ":" + alfrescoJvmConsoleRunConfiguration.port

                CompilerManager.getInstance(project).make(project, project.allModules().toTypedArray()) { aborted, errors, _, _ ->
                    if (successfulCompilation(aborted, errors)) {
                        executeOnAlfresco(project, qualifiedClassName, methodName, httpAddress)
                    }
                }
            } catch (e: RuntimeConfigurationException) {
                RunDialog.editConfiguration(project, runnerAndConfigurationSettings, "Edit configuration")
            }
        }

    private fun executeOnAlfresco(project: Project, qualifiedClassName: String, methodName: String, httpAddress: String) {
        val startTimestamp = currentTimeMillis()

        val runToolWindowTab = RunToolWindowTab(project)
            .also { it.logStart(createTabName(qualifiedClassName, methodName), httpAddress) }

        try {
            val executor = Executors.newSingleThreadExecutor()
            try {
                executor.submit {
                    val result = HttpExecutor().execute(ByteArray(0), qualifiedClassName, methodName)
                    handleSuccessfulExecution(runToolWindowTab, result, currentTimeMillis() - startTimestamp)
                }.also { future -> runToolWindowTab.onClose { future.cancel(true) } }
            } finally {
                executor.shutdown()
            }
        } catch (e: Throwable) {
            handleFailureThrowable(runToolWindowTab, e)
        }
    }

    private fun successfulCompilation(aborted: Boolean, errors: Int): Boolean =
        !aborted && errors == 0

    private fun createTabName(qualifiedClassName: String, methodName: String): String =
        "${qualifiedClassName.split(".").last()}.$methodName"

    private fun handleSuccessfulExecution(runToolWindowTab: RunToolWindowTab, result: List<String>, executionTimeMillis: Long) {
        invokeLater {
            runToolWindowTab.logSuccess(result, executionTimeMillis)
        }
    }

    private fun handleFailureThrowable(runToolWindowTab: RunToolWindowTab, throwable: Throwable) {
        runToolWindowTab.logFailureThrowable(throwable.toFullString())
    }
}