package pl.skotar.intellij.plugin.alfrescojvmconsole.toolwindow

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.icons.AllIcons

internal fun RunToolWindowTab.logStart(tabName: String, address: String) {
    create(tabName)
    setIcon(AllIcons.RunConfigurations.TestState.Run)
    show()

    println("Executing on Alfresco <$address>...")
}

internal fun RunToolWindowTab.newLine() {
    println()
}

internal fun RunToolWindowTab.logExecutionTime(executionTimeMillis: Long) {
    println("Executed in <${calculateTimeInSeconds(executionTimeMillis)} s>")
}

internal fun RunToolWindowTab.logSuccess(messages: List<String>) {
    setIcon(AllIcons.RunConfigurations.TestPassed)

    messages.forEach {
        print(it, ConsoleViewContentType.USER_INPUT)
        println()
    }
    scrollToTheBeginning()
}

internal fun RunToolWindowTab.logFailureThrowable(exceptionString: String) {
    setIcon(AllIcons.RunConfigurations.TestError)

    if (notEndsWithDoubleNewLine()) {
        println()
    }
    printlnError(exceptionString)
    scrollToTheBeginning()
}

private fun RunToolWindowTab.notEndsWithDoubleNewLine(): Boolean {
    flush()
    return !getText().endsWith("\n\n")
}

private fun calculateTimeInSeconds(millis: Long): String =
    String.format("%.3f", millis / 1000.0)