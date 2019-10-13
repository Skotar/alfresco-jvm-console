package pl.skotar.intellij.plugin.alfrescojvmconsole.configuration

import com.intellij.openapi.options.SettingsEditor
import pl.skotar.intellij.plugin.alfrescojvmconsole.configuration.AlfrescoJvmConsoleRunConfigurationValidator.Port
import javax.swing.*

class AlfrescoJvmConsoleSettingsEditor : SettingsEditor<AlfrescoJvmConsoleRunConfiguration>() {

    internal lateinit var panel: JPanel
    internal lateinit var hostTextField: JTextField
    internal lateinit var pathTextField: JTextField
    internal lateinit var portSpinner: JSpinner
    internal lateinit var usernameTextField: JTextField
    internal lateinit var passwordField: JPasswordField

    override fun createEditor(): JComponent {
        portSpinner.setEditorWithoutGroupingAndModel(Port.RANGE.first, Port.RANGE.last)

        return panel
    }

    private fun JSpinner.setEditorWithoutGroupingAndModel(minimum: Int, maximum: Int) {
        model = SpinnerNumberModel(minimum, minimum, maximum, 1)
        editor = JSpinner.NumberEditor(this, "#")
    }

    override fun resetEditorFrom(configuration: AlfrescoJvmConsoleRunConfiguration) {
        hostTextField.text = configuration.host
        pathTextField.text = configuration.path
        portSpinner.value = configuration.port
        usernameTextField.text = configuration.username
        passwordField.text = configuration.password
    }

    override fun applyEditorTo(configuration: AlfrescoJvmConsoleRunConfiguration) {
        configuration.host = hostTextField.text
        configuration.path = pathTextField.text
        configuration.port = portSpinner.value.toString().toInt()
        configuration.username = usernameTextField.text
        configuration.password = String(passwordField.password)
    }
}