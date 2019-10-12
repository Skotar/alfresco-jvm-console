package pl.skotar.intellij.plugin.alfrescojvmconsole.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class AlfrescoJvmConsoleConfigurationFactory(
    type: ConfigurationType
) : ConfigurationFactory(type) {

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        AlfrescoJvmConsoleRunConfiguration(project, this, "Alfresco JVM Console")
}
