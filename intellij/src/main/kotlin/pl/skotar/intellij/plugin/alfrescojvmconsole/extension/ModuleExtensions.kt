package pl.skotar.intellij.plugin.alfrescojvmconsole.extension

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.vfs.VirtualFile

fun Project.getActiveModule(): Module =
    getModule(getActiveFile())

fun Project.getActiveFileOrNull(): VirtualFile? =
    try {
        getActiveFile()
    } catch (e: IllegalStateException) {
        null
    }

fun Project.getModule(file: VirtualFile): Module =
    getModuleForFile(file) ?: throw IllegalStateException("No active module")

private fun Project.getModuleForFile(file: VirtualFile): Module? =
    ModuleManager.getInstance(this).modules.firstOrNull { it.moduleScope.contains(file) }

fun Module.getOutputFolders(): List<VirtualFile> {
    return CompilerModuleExtension.getInstance(this)
        ?.getOutputRoots(true)
        ?.toList()
        ?: throw IllegalStateException("There is no CompilerModuleExtension instance available")
}

fun Module.getCompilerOutputFolder(): VirtualFile {
    return CompilerModuleExtension.getInstance(this)
        ?.compilerOutputPath
        ?: throw IllegalStateException("There is no CompilerModuleExtension instance available or compiler output folder isn't set")
}
