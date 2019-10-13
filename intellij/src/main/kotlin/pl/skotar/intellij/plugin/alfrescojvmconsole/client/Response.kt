package pl.skotar.intellij.plugin.alfrescojvmconsole.client

internal class Response(
    val successfully: Boolean,
    val exception: ExceptionResponse?,
    val messages: List<String>?
) {

    internal class ExceptionResponse(
        val canonicalClassName: String,
        val message: String,
        val stackTrace: List<String>
    )
}