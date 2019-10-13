package pl.skotar.intellij.plugin.alfrescojvmconsole.client

import com.google.gson.Gson
import org.apache.http.HttpHeaders.AUTHORIZATION
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception.ClientException
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception.ClientExecutionException
import java.net.HttpURLConnection.*
import java.util.*

class HttpModuleClient {

    companion object {
        private const val EXECUTE_WEBSCRIPT_PATH = "/service/jvm-console/execute"
        private const val PARAMETER_CANONICAL_CLASS_NAME = "canonicalClassName"
        private const val PARAMETER_FUNCTION_NAME = "functionName"

        private val httpClientBuilder by lazy { HttpClientBuilder.create().build() }

        private val gson = Gson()
    }

    fun execute(
        byteCode: ByteArray,
        canonicalClassName: String,
        functionName: String,
        httpAddress: String,
        username: String,
        password: String
    ): List<String> =
        httpClientBuilder.execute(
            HttpPost(httpAddress + EXECUTE_WEBSCRIPT_PATH + createUrlParameters(canonicalClassName, functionName))
                .setAuthorization(username, password)
                .setByteArrayEntity(byteCode)
        ).use { response ->
            val entityString = response.getEntityString()

            validate(response.statusLine.statusCode, entityString)

            return processResponse(gson.fromJson(entityString, Response::class.java))
        }

    private fun CloseableHttpResponse.getEntityString(): String =
        EntityUtils.toString(entity)

    private fun validate(statusCode: Int, entityString: String) {
        if (statusCode == HTTP_OK) {
            return
        }

        when (statusCode) {
            HTTP_UNAUTHORIZED -> throw ClientException("Bad credentials. Check if the given user has administrator permissions")
            HTTP_NOT_FOUND -> throw ClientException("Alfresco Content Services not found. Check if the given parameters point to the running server")
            else -> throw ClientException(entityString)
        }
    }

    private fun createUrlParameters(canonicalClassName: String, functionName: String): String =
        "?$PARAMETER_CANONICAL_CLASS_NAME=$canonicalClassName&$PARAMETER_FUNCTION_NAME=$functionName"

    private fun HttpPost.setAuthorization(username: String, password: String): HttpPost =
        this.apply {
            val encoding = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
            setHeader(AUTHORIZATION, "Basic $encoding")
        }

    private fun HttpPost.setByteArrayEntity(byteCode: ByteArray): HttpPost =
        this.apply {
            entity = ByteArrayEntity(byteCode, APPLICATION_OCTET_STREAM)
        }

    private fun processResponse(response: Response): List<String> =
        if (response.successfully) {
            response.messages!!
        } else {
            throw ClientExecutionException(response.exception!!.stackTrace.joinToString("\n"))
        }
}