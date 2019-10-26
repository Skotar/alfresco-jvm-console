package pl.skotar.alfresco.module.jvmconsole.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData;
import pl.skotar.alfresco.module.jvmconsole.internal.Executor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static pl.skotar.alfresco.module.jvmconsole.delivery.RequestModel.*;

public class ExecutePostWebScript extends AbstractWebScript {

    private final Executor executor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExecutePostWebScript(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        RequestValidator.validateContentType(request);
        RequestValidator.validateCanonicalClassNameParameter(request);
        RequestValidator.validateFunctionNameParameter(request);
        RequestValidator.validateUseMainClassLoaderParameter(request);

        String canonicalClassName = request.getParameter(PARAMETER_CANONICAL_CLASS_NAME);
        String functionName = request.getParameter(PARAMETER_FUNCTION_NAME);
        boolean useMainClassLoader = getParameterUseMainClassLoader(request);

        List<FormData.FormField> fields = getFields(request);
        RequestValidator.validateFields(fields);

        try {
            List<String> messages = executor.execute(determineClassByteCodes(fields), canonicalClassName, functionName, useMainClassLoader);

            setResponseContentHeader(response);
            objectMapper.writeValue(response.getOutputStream(), Response.of(messages));
        } catch (IllegalArgumentException e) {
            throw new WebScriptException(HTTP_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            setResponseContentHeader(response);
            objectMapper.writeValue(response.getOutputStream(), Response.of(unwrapException(e)));
        }
    }

    private boolean getParameterUseMainClassLoader(WebScriptRequest request) {
        String useMainClassLoader = request.getParameter(PARAMETER_USE_MAIN_CLASS_LOADER);
        if (useMainClassLoader != null) {
            return Boolean.parseBoolean(useMainClassLoader);
        } else {
            return false;
        }
    }

    private List<FormData.FormField> getFields(WebScriptRequest request) {
        FormData formData = (FormData) request.parseContent();
        return Arrays.asList(formData.getFields());
    }

    private List<Executor.ClassByteCode> determineClassByteCodes(List<FormData.FormField> fields) {
        return fields.stream()
                     .map(it -> new Executor.ClassByteCode(it.getName(), readBytes(it)))
                     .collect(Collectors.toList());
    }

    private void setResponseContentHeader(WebScriptResponse response) {
        response.setContentType(APPLICATION_JSON.getMimeType());
        response.setContentEncoding(APPLICATION_JSON.getCharset().displayName());
    }

    private byte[] readBytes(FormData.FormField field) {
        try {
            return IOUtils.toByteArray(field.getContent().getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Throwable unwrapException(Exception e) {
        if (e instanceof InvocationTargetException) {
            return ((InvocationTargetException) e).getTargetException();
        } else {
            return e;
        }
    }
}
