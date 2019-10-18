package pl.skotar.intellij.plugin.alfrescojvmconsole.client

import com.google.gson.Gson
import org.apache.http.HttpHeaders.AUTHORIZATION
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.ClassByteCode
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.ClassDescriptor
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.HttpConfigurationParameters
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception.ClientException
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception.ClientExecutionException
import java.net.HttpURLConnection.*
import java.util.*

internal class HttpModuleClient {

    companion object {
        private const val EXECUTE_WEBSCRIPT_PATH = "/service/jvm-console/execute"
        private const val PARAMETER_CANONICAL_CLASS_NAME = "canonicalClassName"
        private const val PARAMETER_FUNCTION_NAME = "functionName"

        private val applicationJavaVmMimetypeContentType = ContentType.create("application/java-vm")

        private val httpClientBuilder by lazy { HttpClientBuilder.create().build() }

        private val gson = Gson()
    }

    fun execute(
        classDescriptor: ClassDescriptor,
        httpConfigurationParameters: HttpConfigurationParameters,
        classByteCodes: List<ClassByteCode>
    ): List<String> =
        httpClientBuilder.execute(
            HttpPost(
                httpConfigurationParameters.getAddress() + EXECUTE_WEBSCRIPT_PATH +
                        createUrlParameters(classDescriptor.canonicalClassName, classDescriptor.functionName)
            )
                .setAuthorization(httpConfigurationParameters.username, httpConfigurationParameters.password)
                .setMultiPartEntity(classByteCodes)
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
            HTTP_NOT_FOUND -> throw ClientException("Alfresco Content Services or alfresco-jvm-console AMP not found. Check if the given parameters point to the running server and alfresco-jvm-console AMP is installed")
            HTTP_INTERNAL_ERROR -> throw ClientException("An error occurred on Alfresco Content Services. Check logs for more details")
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

    private fun HttpPost.setMultiPartEntity(classByteCodes: List<ClassByteCode>): HttpPost =
        this.apply {
            entity = MultipartEntityBuilder.create().apply {
                setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                classByteCodes.forEach { (canonicalClassName, byteCode) ->
                    addBinaryBody(canonicalClassName, byteCode, applicationJavaVmMimetypeContentType, null)
                }
            }.build()
        }

    private fun processResponse(response: Response): List<String> =
        if (response.successfully) {
            response.messages!!
        } else {
            throw ClientExecutionException(response.exception!!.stackTrace.joinToString("\n"))
        }
}