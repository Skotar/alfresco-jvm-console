package pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception

class HttpException(
    val statusCode: Int,
    val exceptionMessage: String,
    cause: Throwable? = null
) : RuntimeException(
    "Couldn't make request\n" +
            "Status code: $statusCode\n" +
            "Message: ${determineExceptionMessage(exceptionMessage)}",
    cause
)

private fun determineExceptionMessage(exceptionMessage: String): String =
    if (exceptionMessage.isNotBlank()) {
        exceptionMessage
    } else {
        "no message"
    }