package pl.skotar.alfresco.module.jvmconsole.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import pl.skotar.alfresco.module.jvmconsole.internal.Executor;

import java.io.IOException;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;

public class JvmConsoleExecutePostWebScript extends AbstractWebScript {

    private static final String PARAMETER_CANONICAL_CLASS_NAME = "canonicalClassName";
    private static final String PARAMETER_FUNCTION_NAME = "functionName";

    private static final String OCTET_APPLICATION_OCTET_STREAM = APPLICATION_OCTET_STREAM.getMimeType();


    private final Executor executor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JvmConsoleExecutePostWebScript(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) {
        validate(request);

        String canonicalClassName = getCanonicalClassNameParameter(request);
        String functionName = getFunctionNameParameter(request);

        try {
            byte[] byteCode = IOUtils.toByteArray(request.getContent().getInputStream());
            List<String> result = executor.execute(byteCode, canonicalClassName, functionName);
            writeResponse(response, result);
        } catch (Throwable throwable) {
            throw new WebScriptException(HTTP_INTERNAL_ERROR, throwable.getMessage(), throwable);
        }
    }

    private void validate(WebScriptRequest request) {
        String contentType = request.getContentType();
        if (!contentType.equals(OCTET_APPLICATION_OCTET_STREAM)) {
            throw new WebScriptException(HTTP_BAD_REQUEST, "Content-Type is <" + contentType + "> but should be <" + OCTET_APPLICATION_OCTET_STREAM + ">");
        }
    }

    private String getCanonicalClassNameParameter(WebScriptRequest req) {
        String parameter = req.getParameter(PARAMETER_CANONICAL_CLASS_NAME);
        if (parameter != null) {
            return parameter;
        } else {
            throw new WebScriptException(HTTP_BAD_REQUEST, "There is no <" + PARAMETER_CANONICAL_CLASS_NAME + "> parameter");
        }
    }

    private String getFunctionNameParameter(WebScriptRequest req) {
        String parameter = req.getParameter(PARAMETER_FUNCTION_NAME);
        if (parameter != null) {
            return parameter;
        } else {
            throw new WebScriptException(HTTP_BAD_REQUEST, "There is no <" + PARAMETER_FUNCTION_NAME + "> parameter");
        }
    }

    private void writeResponse(WebScriptResponse response, List<String> result) throws IOException {
        response.setContentType(APPLICATION_JSON.getMimeType());
        response.setContentEncoding(APPLICATION_JSON.getCharset().displayName());
        objectMapper.writeValue(response.getOutputStream(), result);
    }
}
