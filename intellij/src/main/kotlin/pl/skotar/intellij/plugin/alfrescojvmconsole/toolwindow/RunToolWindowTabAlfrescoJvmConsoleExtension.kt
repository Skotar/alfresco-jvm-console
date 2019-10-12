package pl.skotar.intellij.plugin.alfrescojvmconsole.toolwindow

import com.intellij.icons.AllIcons

internal fun RunToolWindowTab.logStart(tabName: String, httpAddress: String) {
    create(tabName)
    setIcon(AllIcons.RunConfigurations.TestState.Run)
    show()

    println("Executing on Alfresco <$httpAddress>...")
}

internal fun RunToolWindowTab.newLine() {
    println()
}

internal fun RunToolWindowTab.logSuccess(result: List<String>, executionTimeMillis: Long) {
    println(
        "Executed in <${calculateTimeInSeconds(executionTimeMillis)} s>" + if (result.isNotEmpty()) ":" else ""
    )
    result.forEach {
        print(it)
        println()
    }
    println("")
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