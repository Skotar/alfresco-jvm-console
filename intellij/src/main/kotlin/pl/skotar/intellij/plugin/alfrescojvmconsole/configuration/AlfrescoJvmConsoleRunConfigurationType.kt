package pl.skotar.intellij.plugin.alfrescojvmconsole.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import javax.swing.Icon

object AlfrescoJvmConsoleRunConfigurationType : ConfigurationType {

    override fun getDisplayName(): String =
        "Alfresco JVM Console"

    override fun getConfigurationTypeDescription(): String? =
        null

    override fun getIcon(): Icon? =
        null

    override fun getId(): String =
        "ALFRESCO_JVM_CONSOLE_RUN_CONFIGURATION"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(AlfrescoJvmConsoleConfigurationFactory(this))
}
