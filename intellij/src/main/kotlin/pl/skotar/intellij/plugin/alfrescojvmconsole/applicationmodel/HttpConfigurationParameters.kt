package pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel

internal data class HttpConfigurationParameters(
    val host: String,
    val path: String,
    val port: Int,
    val username: String,
    val password: String
) {

    fun getAddress(): String =
        "http://" + host.removeSuffix("/") + ":" + port + "/" + path.removePrefix("/").removeSuffix("/")
}