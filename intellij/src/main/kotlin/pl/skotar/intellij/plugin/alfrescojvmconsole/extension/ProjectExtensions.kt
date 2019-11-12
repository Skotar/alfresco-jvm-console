package pl.skotar.intellij.plugin.alfrescojvmconsole.extension

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.impl.RunDialog
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

fun Project.getActiveFile(): VirtualFile =
    getEditor().file ?: throw IllegalStateException("No file open")

fun Project.getEditor(): FileEditor =
    FileEditorManager.getInstance(this).selectedEditors.firstOrNull { it is TextEditor }
        ?: throw IllegalStateException("No text editor or file opened")

fun Project.isFileInAnyModule(file: VirtualFile): Boolean =
    getModuleForFile(file) != null

private fun Project.getModuleForFile(file: VirtualFile): Module? =
    ModuleManager.getInstance(this).modules.firstOrNull { it.moduleScope.contains(file) }

fun Project.editConfiguration(runnerAndConfigurationSettings: RunnerAndConfigurationSettings) {
    RunDialog.editConfiguration(this, runnerAndConfigurationSettings, "Edit configuration")
}