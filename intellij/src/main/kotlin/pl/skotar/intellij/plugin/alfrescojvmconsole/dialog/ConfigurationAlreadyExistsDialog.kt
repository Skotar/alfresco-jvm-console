package pl.skotar.intellij.plugin.alfrescojvmconsole.dialog

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import pl.skotar.intellij.plugin.alfrescojvmconsole.dialog.ConfigurationAlreadyExistsDialog.Result.CREATE_NEW
import pl.skotar.intellij.plugin.alfrescojvmconsole.dialog.ConfigurationAlreadyExistsDialog.Result.USE_SELECTED
import javax.swing.*

class ConfigurationAlreadyExistsDialog(
    project: Project,
    private val configurations: List<RunnerAndConfigurationSettings>
) : DialogWrapper(project, false) {

    enum class Result {
        CREATE_NEW,
        USE_SELECTED
    }

    internal lateinit var result: Result
    internal var selectedConfiguration: RunnerAndConfigurationSettings = configurations.first()

    internal lateinit var panel: JPanel
    internal lateinit var configurationsList: JList<String>

    internal lateinit var useSelectedButton: JButton
    internal lateinit var createNewButton: JButton
    internal lateinit var buttonPanel: JPanel

    init {
        init()

        setUpCreateNewButton()
        setUpUseSelectedButton()
        setUpConfigurationsList()
    }

    private fun setUpCreateNewButton() {
        createNewButton.addActionListener {
            result = CREATE_NEW
            close(0)
        }
    }

    private fun setUpUseSelectedButton() {
        configurationsList.addListSelectionListener {
            useSelectedButton.actionListeners.forEach { useSelectedButton.removeActionListener(it) }
            useSelectedButton.addActionListener {
                result = USE_SELECTED
                selectedConfiguration = configurations[configurationsList.selectedIndex]
                close(0)
            }
        }
    }

    private fun setUpConfigurationsList() {
        val model = configurationsList.model as DefaultListModel<String>
        configurations.map(RunnerAndConfigurationSettings::getName).forEach { model.addElement(it) }
        configurationsList.selectedIndex = 0
    }

    override fun createCenterPanel(): JComponent =
        panel

    override fun createButtonsPanel(buttons: MutableList<out JButton>): JPanel =
        buttonPanel
}