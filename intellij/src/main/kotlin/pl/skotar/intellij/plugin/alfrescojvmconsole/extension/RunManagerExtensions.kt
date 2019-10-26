package pl.skotar.intellij.plugin.alfrescojvmconsole.extension

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleConfigurationFactory
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleRunConfiguration
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleRunConfigurationType

internal fun RunManager.getSelectedAlfrescoJvmConsoleRunnerAndConfigurationSettings(): RunnerAndConfigurationSettings? {
    val selectedConfiguration = selectedConfiguration
    return if (selectedConfiguration != null && selectedConfiguration.configuration is AlfrescoJvmConsoleRunConfiguration) selectedConfiguration else null
}

internal fun RunManager.getAlfrescoJvmConsoleRunnerAndConfigurationSettings(): List<RunnerAndConfigurationSettings> =
    allSettings.filter { it.configuration is AlfrescoJvmConsoleRunConfiguration }

internal fun RunManager.createAlfrescoJvmConsoleRunnerAndConfigurationSettings(): RunnerAndConfigurationSettings {
    val uniqueName = suggestUniqueName(null, AlfrescoJvmConsoleRunConfigurationType)
    return createConfiguration(
        uniqueName,
        AlfrescoJvmConsoleConfigurationFactory(AlfrescoJvmConsoleRunConfigurationType)
    )
        .also(::addConfiguration)
        .also(::setTemporaryConfiguration)
}