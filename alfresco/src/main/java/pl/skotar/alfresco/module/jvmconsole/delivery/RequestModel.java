package pl.skotar.alfresco.module.jvmconsole.delivery;

import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;

class RequestModel {

    private RequestModel() {
    }

    static final String PARAMETER_CANONICAL_CLASS_NAME = "canonicalClassName";
    static final String PARAMETER_FUNCTION_NAME = "functionName";
    static final String PARAMETER_USE_MAIN_CLASS_LOADER = "useMainClassLoader";

    static final String MULTIPART_FORM_DATA_MIMETYPE = MULTIPART_FORM_DATA.getMimeType();
}
