package pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel

internal data class ClassDescriptor(
    val packageName: String,
    val className: String,
    val functionName: String
) {

    val canonicalClassName = "$packageName.$className"
}