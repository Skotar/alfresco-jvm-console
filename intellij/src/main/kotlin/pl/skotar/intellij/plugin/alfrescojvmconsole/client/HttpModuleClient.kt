package pl.skotar.intellij.plugin.alfrescojvmconsole.client

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.BlobDataPart
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.github.kittinunf.fuel.gson.gsonDeserializer
import com.google.gson.Gson
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.ClassByteCode
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.ClassDescriptor
import pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel.HttpConfigurationParameters
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception.ExecutionException
import pl.skotar.intellij.plugin.alfrescojvmconsole.client.exception.HttpException

internal class HttpModuleClient {

    data class ErrorResponse(
        val message: String
    )

    companion object {
        private const val EXECUTE_WEBSCRIPT_PATH = "/service/jvm-console/execute"
        private const val PARAMETER_CANONICAL_CLASS_NAME = "canonicalClassName"
        private const val PARAMETER_FUNCTION_NAME = "functionName"
        private const val PARAMETER_USE_MAIN_CLASS_LOADER = "useMainClassLoader"

        private const val APPLICATION_JAVA_VM_MIMETYPE = "application/java-vm"
    }

    suspend fun execute(
        classDescriptor: ClassDescriptor,
        httpConfigurationParameters: HttpConfigurationParameters,
        classByteCodes: List<ClassByteCode>,
        useMainClassLoader: Boolean
    ): List<String> =
        try {
            Fuel.upload(
                httpConfigurationParameters.getAddress() + EXECUTE_WEBSCRIPT_PATH,
                parameters = createUrlParameters(classDescriptor.canonicalClassName, classDescriptor.functionName, useMainClassLoader)
            )
                .add(*determineBlobDataParts(classByteCodes))
                .authentication().basic(httpConfigurationParameters.username, httpConfigurationParameters.password)
                .timeout(Int.MAX_VALUE)
                .timeoutRead(Int.MAX_VALUE)
                .awaitObject(gsonDeserializer<Response>())
                .let(::processResponse)
        } catch (e: FuelError) {
            throw HttpException(e.response.statusCode, extractMessage(e.errorData), e)
        }

    private fun createUrlParameters(canonicalClassName: String, functionName: String, useMainClassLoader: Boolean): List<Pair<String, Any>> =
        listOf(
            PARAMETER_CANONICAL_CLASS_NAME to canonicalClassName,
            PARAMETER_FUNCTION_NAME to functionName,
            PARAMETER_USE_MAIN_CLASS_LOADER to useMainClassLoader
        )

    private fun determineBlobDataParts(classByteCodes: List<ClassByteCode>): Array<BlobDataPart> =
        classByteCodes.map { (canonicalClassName, byteCode) ->
            BlobDataPart(byteCode.inputStream(), name = canonicalClassName, contentType = APPLICATION_JAVA_VM_MIMETYPE)
        }.toTypedArray()

    private fun processResponse(response: Response): List<String> =
        if (response.successfully) {
            response.messages!!
        } else {
            throw ExecutionException(response.exception!!.stackTrace.joinToString("\n"))
        }

    private fun extractMessage(data: ByteArray): String {
        val stringData = String(data)
        return try {
            Gson().fromJson(stringData, ErrorResponse::class.java)
                .message
        } catch (e: Exception) {
            stringData
        }
    }
}