package pl.skotar.intellij.plugin.alfrescojvmconsole.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object AlfrescoJvmConsoleRunConfigurationType : ConfigurationType {

    private val icon = IconLoader.getIcon("/icon/alfresco.svg", AlfrescoJvmConsoleRunConfigurationType::class.java)

    override fun getDisplayName(): String =
        "Alfresco JVM Console"

    override fun getConfigurationTypeDescription(): String? =
        null

    override fun getIcon(): Icon =
        icon

    override fun getId(): String =
        "ALFRESCO_JVM_CONSOLE_RUN_CONFIGURATION"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(AlfrescoJvmConsoleConfigurationFactory(this))
}
