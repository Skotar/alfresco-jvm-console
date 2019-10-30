package pl.skotar.alfresco.module.jvmconsole.delivery;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Response {

    private static class ExceptionResponse {

        private final String canonicalClassName;
        private final String message;
        private final List<String> stackTrace;

        private ExceptionResponse(String canonicalClassName, String message, List<String> stackTrace) {
            this.canonicalClassName = canonicalClassName;
            this.message = message;
            this.stackTrace = stackTrace;
        }

        public String getCanonicalClassName() {
            return canonicalClassName;
        }

        public String getMessage() {
            return message;
        }

        public List<String> getStackTrace() {
            return stackTrace;
        }
    }

    private final Boolean successfully;
    private final ExceptionResponse exception;
    private final List<String> messages;

    private Response(Boolean successfully, ExceptionResponse exception, List<String> messages) {
        this.successfully = successfully;
        this.exception = exception;
        this.messages = messages;
    }

    public static Response of(List<String> messages) {
        return new Response(true, null, messages);
    }

    public static Response of(Throwable throwable) {
        return new Response(false,
                            new ExceptionResponse(throwable.getClass().getCanonicalName(),
                                                  throwable.getMessage(),
                                                  convertStackTraceAndRemoveReflectionDetails(throwable)),
                            null);
    }

    private static List<String> convertStackTraceAndRemoveReflectionDetails(Throwable throwable) {
        return ExceptionUtils.getThrowableList(throwable).stream()
                             .map(it -> removeReflectionDetails(ExceptionUtils.getStackTrace(it).split("\n")))
                             .flatMap(Collection::stream)
                             .collect(Collectors.toList());
    }

    private static List<String> removeReflectionDetails(String[] stackTrace) {
        List<String> limitedStackTrace = new ArrayList<>();
        for (int i = 0; i < stackTrace.length && !stackTrace[i].contains("jdk.internal.reflect.NativeMethodAccessorImpl.invoke0"); i++) {
            limitedStackTrace.add(stackTrace[i]);
        }
        return limitedStackTrace;
    }

    public Boolean getSuccessfully() {
        return successfully;
    }

    public ExceptionResponse getException() {
        return exception;
    }

    public List<String> getMessages() {
        return messages;
    }
}
