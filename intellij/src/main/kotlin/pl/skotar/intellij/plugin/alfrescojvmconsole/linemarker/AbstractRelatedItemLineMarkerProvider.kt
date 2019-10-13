package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.execution.RunManager
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.execution.impl.RunDialog
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.HttpModuleClient
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception.ClientException
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception.ClientExecutionException
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleRunConfiguration
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.*
import pl.skotar.intellij.plugin.alfrescojvmconsole.toolwindow.*
import pl.skotar.intellij.plugin.alfrescojvmconsole.util.invokeLater
import java.lang.System.currentTimeMillis
import java.util.concurrent.Executors

abstract class AbstractRelatedItemLineMarkerProvider {

    companion object {
        private val moduleClient = HttpModuleClient()
    }

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

                val runConfiguration = runnerAndConfigurationSettings.configuration as AlfrescoJvmConsoleRunConfiguration
                val httpAddress = createHttpAddress(runConfiguration)

                CompilerManager.getInstance(project).make(project, project.allModules().toTypedArray()) { aborted, errors, _, _ ->
                    if (successfulCompilation(aborted, errors)) {
                        project.getActiveModule().getCompilerOutputFolder().refresh(true, true) {
                            executeOnAlfresco(project, qualifiedClassName, methodName, httpAddress, runConfiguration.username, runConfiguration.password)
                        }
                    }
                }
            } catch (e: RuntimeConfigurationException) {
                RunDialog.editConfiguration(project, runnerAndConfigurationSettings, "Edit configuration")
            }
        }

    private fun createHttpAddress(runConfiguration: AlfrescoJvmConsoleRunConfiguration): String =
        "http://" + runConfiguration.host.removeSuffix("/") + ":" + runConfiguration.port + "/" + runConfiguration.path.removePrefix("/").removeSuffix("/")

    private fun successfulCompilation(aborted: Boolean, errors: Int): Boolean =
        !aborted && errors == 0

    private fun executeOnAlfresco(
        project: Project,
        qualifiedClassName: String,
        methodName: String,
        httpAddress: String,
        username: String,
        password: String
    ) {
        val startTimestamp = currentTimeMillis()

        ApplicationManager.getApplication().runWriteAction {
            val runToolWindowTab = RunToolWindowTab(project)
                .also { it.logStart(createTabName(qualifiedClassName, methodName), httpAddress) }

            val executor = Executors.newSingleThreadExecutor()
            try {
                val byteCode = getByteCode(project, qualifiedClassName)

                executor.submit {
                    try {
                        val messages = moduleClient.execute(byteCode, qualifiedClassName, methodName, httpAddress, username, password)
                        handleSuccessfulExecution(runToolWindowTab, messages, startTimestamp)
                    } catch (e: Exception) {
                        handleFailureExecution(runToolWindowTab, e, startTimestamp)
                    }
                }.also { future -> runToolWindowTab.onClose { future.cancel(true) } }
            } catch (e: Throwable) {
                handleFailureExecution(runToolWindowTab, e, startTimestamp)
            } finally {
                executor.shutdown()
            }
        }
    }

    private fun createTabName(qualifiedClassName: String, methodName: String): String =
        "${qualifiedClassName.split(".").last()}.$methodName"

    private fun getByteCode(project: Project, qualifiedClassName: String): ByteArray {
        val relativePath = qualifiedClassName.replace(".", "/") + ".class"
        return project.getActiveModule()
            .getOutputFolders()
            .mapNotNull { it.findFileByRelativePath(relativePath) }
            .firstOrNull()
            ?.inputStream?.readBytes()
            ?: throw IllegalStateException("There is no <$qualifiedClassName> class in target folder")
    }

    private fun handleSuccessfulExecution(runToolWindowTab: RunToolWindowTab, messages: List<String>, startTimestamp: Long) {
        invokeLater {
            runToolWindowTab.logExecutionTime(currentTimeMillis() - startTimestamp)
            runToolWindowTab.newLine()
            runToolWindowTab.logSuccess(messages)
        }
    }

    private fun handleFailureExecution(runToolWindowTab: RunToolWindowTab, throwable: Throwable, startTimestamp: Long) {
        invokeLater {
            when (throwable) {
                is ClientExecutionException -> {
                    runToolWindowTab.logExecutionTime(currentTimeMillis() - startTimestamp)
                    runToolWindowTab.newLine()
                    runToolWindowTab.logFailureThrowable(throwable.message!!)
                }
                is ClientException -> {
                    runToolWindowTab.newLine()
                    runToolWindowTab.logFailureThrowable(throwable.message!!)
                }
                else -> {
                    runToolWindowTab.newLine()
                    runToolWindowTab.logFailureThrowable(throwable.toFullString())
                }
            }
        }
    }
}