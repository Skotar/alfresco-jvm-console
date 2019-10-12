package pl.skotar.intellij.plugin.alfrescojvmconsole.util

import com.intellij.openapi.application.ApplicationManager

fun invokeLater(toRun: () -> Unit) {
    ApplicationManager.getApplication().invokeLater {
        toRun()
    }
}