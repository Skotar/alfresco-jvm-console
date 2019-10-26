package pl.skotar.alfresco.module.jvmconsole.delivery;

import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

import java.util.List;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static pl.skotar.alfresco.module.jvmconsole.delivery.RequestModel.*;

class RequestValidator {

    private RequestValidator() {
    }

    static void validateContentType(WebScriptRequest request) {
        String contentType = request.getContentType();
        if (!contentType.equals(MULTIPART_FORM_DATA_MIMETYPE)) {
            throw new WebScriptException(HTTP_BAD_REQUEST, "Content-Type is <" + contentType + "> but should be <" + MULTIPART_FORM_DATA_MIMETYPE + ">");
        }
    }

    static void validateCanonicalClassNameParameter(WebScriptRequest request) {
        if (request.getParameter(PARAMETER_CANONICAL_CLASS_NAME) == null) {
            throw new WebScriptException(HTTP_BAD_REQUEST, "There is no <" + PARAMETER_CANONICAL_CLASS_NAME + "> parameter");
        }
    }

    static void validateFunctionNameParameter(WebScriptRequest request) {
        if (request.getParameter(PARAMETER_FUNCTION_NAME) == null) {
            throw new WebScriptException(HTTP_BAD_REQUEST, "There is no <" + PARAMETER_FUNCTION_NAME + "> parameter");
        }
    }

    static void validateUseMainClassLoaderParameter(WebScriptRequest request) {
        String useMainClassLoader = request.getParameter(PARAMETER_USE_MAIN_CLASS_LOADER);
        if (useMainClassLoader != null && isNotBoolean(useMainClassLoader)) {
            throw new WebScriptException(HTTP_BAD_REQUEST, "Parameter <" + PARAMETER_USE_MAIN_CLASS_LOADER + "> must be <true> or <false>");
        }
    }

    private static boolean isNotBoolean(String useMainClassLoader) {
        return !(useMainClassLoader.equals("false") || useMainClassLoader.equals("true"));
    }

    static void validateFields(List<FormData.FormField> fields) {
        for (FormData.FormField field : fields) {
            if (field.getName().isBlank()) {
                throw new WebScriptException(HTTP_BAD_REQUEST, "One of the fields has blank name but should contain canonical class name");
            }
        }
    }
}
