package pl.skotar.intellij.plugin.alfrescojvmconsole.configuration

import com.intellij.execution.ExecutionTarget
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializer
import org.jdom.Element
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleRunConfigurationValidator.Host
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleRunConfigurationValidator.Password
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleRunConfigurationValidator.Path
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleRunConfigurationValidator.Port
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleRunConfigurationValidator.Username

class AlfrescoJvmConsoleRunConfiguration(
    project: Project,
    factory: AlfrescoJvmConsoleConfigurationFactory,
    name: String
) : RunConfigurationBase<AlfrescoJvmConsoleSettingsEditor>(project, factory, name) {

    internal var host: String = "localhost"
    internal var path: String = "alfresco"
    internal var port: Int = 8080
    internal var username: String = "admin"
    internal var password: String = ""

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        AlfrescoJvmConsoleSettingsEditor()

    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState? =
        null

    override fun canRunOn(target: ExecutionTarget): Boolean =
        false

    override fun checkConfiguration() {
        if (!Host.validate(host)) {
            throw RuntimeConfigurationError("Host must be set")
        }

        if (!Path.validate(path)) {
            throw RuntimeConfigurationError("Path must be set")
        }

        if (!Port.validate(port)) {
            throw RuntimeConfigurationError("Port must be in range ${Port.RANGE.toDescription()}")
        }

        if (!Username.validate(username)) {
            throw RuntimeConfigurationError("Username must be set")
        }

        if (!Password.validate(password)) {
            throw RuntimeConfigurationError("Password must be set")
        }
    }

    private fun IntRange.toDescription(): String =
        "[$start, $last]"

    override fun readExternal(element: Element) {
        super.readExternal(element)
        XmlSerializer.deserializeInto(this, element)
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        XmlSerializer.serializeInto(this, element)
    }
}
