package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.execution.RunManager
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.execution.impl.RunDialog
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.ClassByteCode
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.ClassDescriptor
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.HttpConfigurationParameters
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.HttpModuleClient
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception.ClientException
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception.ClientExecutionException
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleRunConfiguration
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.*
import pl.skotar.intellij.plugin.alfrescojvmconsole.toolwindow.*
import pl.skotar.intellij.plugin.alfrescojvmconsole.util.invokeLater
import java.lang.System.currentTimeMillis
import java.util.concurrent.Executors

internal abstract class AbstractRelatedItemLineMarkerProvider {

    companion object {
        private val moduleClient = HttpModuleClient()
    }

    protected fun createOnClickHandler(
        project: Project,
        getClassDescriptor: () -> ClassDescriptor
    ): () -> Unit =
        {
            val runManager = RunManager.getInstance(project)
            val runnerAndConfigurationSettings =
                (runManager.getSelectedAlfrescoJvmConsoleRunnerAndConfigurationSettings()
                    ?: runManager.createAlfrescoJvmConsoleRunnerAndConfigurationSettings())
                    .also { runManager.selectedConfiguration = it }

            try {
                runnerAndConfigurationSettings.checkSettings()

                val classDescriptor = getClassDescriptor()
                val httpConfigurationParameters =
                    createHttpConfigurationParameters(runnerAndConfigurationSettings.configuration as AlfrescoJvmConsoleRunConfiguration)

                CompilerManager.getInstance(project).make(project, project.allModules().toTypedArray()) { aborted, errors, _, _ ->
                    if (successfulCompilation(aborted, errors)) {
                        project.getActiveModule().getCompilerOutputFolder().refresh(true, true) {
                            executeOnAlfresco(project, classDescriptor, httpConfigurationParameters)
                        }
                    }
                }
            } catch (e: RuntimeConfigurationException) {
                RunDialog.editConfiguration(project, runnerAndConfigurationSettings, "Edit configuration")
            }
        }

    private fun createHttpConfigurationParameters(runConfiguration: AlfrescoJvmConsoleRunConfiguration): HttpConfigurationParameters =
        HttpConfigurationParameters(
            runConfiguration.host,
            runConfiguration.path,
            runConfiguration.port,
            runConfiguration.username,
            runConfiguration.password
        )

    private fun successfulCompilation(aborted: Boolean, errors: Int): Boolean =
        !aborted && errors == 0

    private fun executeOnAlfresco(project: Project, classDescriptor: ClassDescriptor, httpConfigurationParameters: HttpConfigurationParameters) {
        val startTimestamp = currentTimeMillis()

//        ApplicationManager.getApplication().runWriteAction {
        val runToolWindowTab = RunToolWindowTab(project)
            .also { it.logStart(createTabName(classDescriptor.className, classDescriptor.functionName), httpConfigurationParameters.getAddress()) }

        val executor = Executors.newSingleThreadExecutor()
        try {
            val classCodeBytes = determineClassCodeBytes(project, classDescriptor)

            executor.submit {
                try {
                    val messages = moduleClient.execute(classDescriptor, httpConfigurationParameters, classCodeBytes)
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
//        }
    }

    private fun createTabName(className: String, functionName: String): String =
        "$className.$functionName"

    private fun determineClassCodeBytes(project: Project, classDescriptor: ClassDescriptor): List<ClassByteCode> {
        val relativePath = classDescriptor.packageName.replace(".", "/")
        val classByteCodes = (project.getActiveModule()
            .getOutputFolders()
            .mapNotNull { it.findFileByRelativePath(relativePath) }
            .firstOrNull()
            ?.children
            ?.filter { it.name.startsWith(classDescriptor.className) }
            ?.map { ClassByteCode(classDescriptor.packageName + "." + it.name.removeSuffix(".class"), it.inputStream.readBytes()) }
            ?: throw IllegalStateException("There is no <$relativePath> folder in target"))

        check(classByteCodes.map(ClassByteCode::canonicalClassName).contains(classDescriptor.canonicalClassName))
        { "There is no <$relativePath/${classDescriptor.className}.class> file in target folder" }

        return classByteCodes
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