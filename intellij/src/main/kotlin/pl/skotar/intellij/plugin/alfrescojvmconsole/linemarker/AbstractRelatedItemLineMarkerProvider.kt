package pl.skotar.intellij.plugin.alfrescojvmconsole.linemarker

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.ClassByteCode
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.ClassDescriptor
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.HttpConfigurationParameters
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.HttpModuleClient
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception.ExecutionException
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception.HttpException
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleRunConfiguration
import pl.skotar.intellij.plugin.alfrescojvmconsole.dialog.ConfigurationAlreadyExistsDialog
import pl.skotar.intellij.plugin.alfrescojvmconsole.dialog.ConfigurationAlreadyExistsDialog.Result.CREATE_NEW
import pl.skotar.intellij.plugin.alfrescojvmconsole.dialog.ConfigurationAlreadyExistsDialog.Result.USE_SELECTED
import pl.skotar.intellij.plugin.alfrescojvmconsole.extension.*
import pl.skotar.intellij.plugin.alfrescojvmconsole.toolwindow.*
import pl.skotar.intellij.plugin.alfrescojvmconsole.util.invokeLater
import java.lang.System.currentTimeMillis
import java.net.ConnectException
import java.net.HttpURLConnection.*
import java.net.UnknownHostException

internal abstract class AbstractRelatedItemLineMarkerProvider {

    companion object {
        private val moduleClient = HttpModuleClient()
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    protected fun createOnClickHandler(
        project: Project,
        getComments: () -> List<String>,
        getClassDescriptor: () -> ClassDescriptor
    ): () -> Unit =
        {
            val runManager = RunManager.getInstance(project)
            val runnerAndConfigurationSettings =
                (runManager.getSelectedAlfrescoJvmConsoleRunnerAndConfigurationSettings() ?: showDialogToUseSelectedOrCreateNew(project, runManager))

            try {
                runnerAndConfigurationSettings.checkSettings()

                val classDescriptor = getClassDescriptor()
                val httpConfigurationParameters =
                    createHttpConfigurationParameters(runnerAndConfigurationSettings.configuration as AlfrescoJvmConsoleRunConfiguration)
                val useMainClassLoader = determineUseMainClassLoader(getComments())

                CompilerManager.getInstance(project).make(project, project.allModules().toTypedArray()) { aborted, errors, _, _ ->
                    if (successfulCompilation(aborted, errors)) {
                        project.getActiveModule().getCompilerOutputFolder().refresh(true, true) {
                            executeOnAlfresco(project, classDescriptor, httpConfigurationParameters, useMainClassLoader)
                        }
                    }
                }
            } catch (e: RuntimeConfigurationException) {
                project.editConfiguration(runnerAndConfigurationSettings)
            }
        }

    private fun showDialogToUseSelectedOrCreateNew(project: Project, runManager: RunManager): RunnerAndConfigurationSettings {
        val configurations = runManager.getAlfrescoJvmConsoleRunnerAndConfigurationSettings()
        return if (configurations.isNotEmpty()) {
            val dialog = ConfigurationAlreadyExistsDialog(project, configurations)
                .also(ConfigurationAlreadyExistsDialog::show)

            when (dialog.result) {
                CREATE_NEW -> runManager.createAlfrescoJvmConsoleRunnerAndConfigurationSettings()
                    .also { project.editConfiguration(it) }
                USE_SELECTED -> dialog.selectedConfiguration
            }
        } else {
            runManager.createAlfrescoJvmConsoleRunnerAndConfigurationSettings()
                .also { project.editConfiguration(it) }
        }.also { runManager.selectedConfiguration = it }
    }

    private fun createHttpConfigurationParameters(runConfiguration: AlfrescoJvmConsoleRunConfiguration): HttpConfigurationParameters =
        HttpConfigurationParameters(
            runConfiguration.host,
            runConfiguration.path,
            runConfiguration.port,
            runConfiguration.username,
            runConfiguration.password
        )

    private fun determineUseMainClassLoader(comments: List<String>): Boolean =
        comments.any { it.contains("useMainClassLoader") }

    private fun successfulCompilation(aborted: Boolean, errors: Int): Boolean =
        !aborted && errors == 0

    private fun executeOnAlfresco(
        project: Project,
        classDescriptor: ClassDescriptor,
        httpConfigurationParameters: HttpConfigurationParameters,
        useMainClassLoader: Boolean
    ) {
        val startTimestamp = currentTimeMillis()

        val runToolWindowTab = RunToolWindowTab(project)
            .also { it.logStart(createTabName(classDescriptor.className, classDescriptor.functionName), httpConfigurationParameters.getAddress()) }

        try {
            val classCodeBytes = determineClassCodeBytes(project, classDescriptor)

            coroutineScope.launch {
                try {
                    moduleClient.execute(classDescriptor, httpConfigurationParameters, classCodeBytes, useMainClassLoader)
                        .also { handleSuccessfulExecution(runToolWindowTab, it, startTimestamp) }
                } catch (e: CancellationException) {
                    // deliberately omitted. Thrown exception isn't printed because the tab was closed
                } catch (e: Exception) {
                    handleFailedExecution(runToolWindowTab, e, startTimestamp)
                }
            }.also { runToolWindowTab.onClose { it.cancel() } }
        } catch (e: Throwable) {
            handleFailedExecution(runToolWindowTab, e, startTimestamp)
        }
    }

    private fun createTabName(className: String, functionName: String): String =
        "$className.$functionName"

    private fun determineClassCodeBytes(project: Project, classDescriptor: ClassDescriptor): List<ClassByteCode> {
        val relativePath = classDescriptor.packageName.replace(".", "/")
        val classByteCodes = project.getActiveModule()
            .getOutputFolders()
            .mapNotNull { it.findFileByRelativePath(relativePath) }
            .flatMap { getClassByteCodesForClassInFolder(it, classDescriptor) }

        check(classByteCodes.map(ClassByteCode::canonicalClassName).contains(classDescriptor.canonicalClassName))
        { "There is no <$relativePath/${classDescriptor.className}.class> file in any output folder. Try to rebuild module manually" }

        return classByteCodes
    }

    private fun getClassByteCodesForClassInFolder(virtualFile: VirtualFile, classDescriptor: ClassDescriptor): List<ClassByteCode> =
        virtualFile.children
            .filter { it.name.startsWith(classDescriptor.className) }
            .map { ClassByteCode(classDescriptor.packageName + "." + it.name.removeSuffix(".class"), it.inputStream.readBytes()) }

    private fun handleSuccessfulExecution(runToolWindowTab: RunToolWindowTab, messages: List<String>, startTimestamp: Long) {
        invokeLater {
            runToolWindowTab.logExecutionTime(currentTimeMillis() - startTimestamp)
            runToolWindowTab.newLine()
            runToolWindowTab.logSuccess(messages)
        }
    }

    private fun handleFailedExecution(runToolWindowTab: RunToolWindowTab, exception: Throwable, startTimestamp: Long) {
        invokeLater {
            when (exception) {
                is ExecutionException -> {
                    runToolWindowTab.logExecutionTime(currentTimeMillis() - startTimestamp)
                    runToolWindowTab.newLine()
                    runToolWindowTab.logFailureThrowable(exception.message!!)
                }
                is HttpException -> {
                    val statusCode = exception.statusCode
                    val message = when {
                        statusCode == HTTP_NOT_FOUND || verifyUnknownResponseCode(exception) -> "Alfresco Content Services or alfresco-jvm-console AMP not found. Check if the given parameters point to the running server and alfresco-jvm-console AMP is installed"
                        statusCode == HTTP_UNAUTHORIZED -> "Bad credentials. Check if the given user has administrator permissions"
                        statusCode == HTTP_INTERNAL_ERROR -> "An error occurred on Alfresco Content Services. Check logs for more details"
                        else -> exception.toFullString()
                    }

                    runToolWindowTab.newLine()
                    runToolWindowTab.logFailureThrowable(message)
                }
                else -> {
                    runToolWindowTab.newLine()
                    runToolWindowTab.logFailureThrowable(exception.toFullString())
                }
            }
        }
    }

    private fun verifyUnknownResponseCode(exception: HttpException): Boolean {
        val exceptionString = exception.toFullString()
        return exception.statusCode == -1 &&
                (exceptionString.contains(UnknownHostException::class.java.canonicalName) || exceptionString.contains(ConnectException::class.java.canonicalName))
    }
}