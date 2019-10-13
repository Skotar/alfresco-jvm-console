package pl.skotar.intellij.plugin.alfrescojvmconsole.executor

import com.google.gson.Gson
import com.google.gson.JsonParser
import org.apache.http.HttpHeaders.AUTHORIZATION
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import java.net.HttpURLConnection.HTTP_OK
import java.util.*

class HttpExecutor {

    companion object {
        private const val EXECUTE_WEBSCRIPT_PATH = "/service/jvm-console/execute"
        private const val PARAMETER_CANONICAL_CLASS_NAME = "canonicalClassName"
        private const val PARAMETER_FUNCTION_NAME = "functionName"

        private val httpClientBuilder by lazy { HttpClientBuilder.create().build() }

        private val gson = Gson()
        private val jsonParser = JsonParser()
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

            return gson.fromJson(entityString, getClazz())
        }

    private fun CloseableHttpResponse.getEntityString(): String =
        EntityUtils.toString(entity)

    private fun validate(statusCode: Int, entityString: String) {
        check(statusCode == HTTP_OK) { "Couldn't execute code because of <$statusCode> error:\n$entityString" }
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

    private inline fun <reified T : Any> getClazz(): Class<T> =
        T::class.java
}