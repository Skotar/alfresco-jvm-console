package pl.skotar.alfresco.module.jvmconsole.delivery;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Response {

    private static class ExceptionResponse {

        private String canonicalClassName;
        private String message;
        private List<String> stackTrace;

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

    private Boolean successfully;
    private ExceptionResponse exception;
    private List<String> messages;

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
                                                  convertStackTraceAndRemoveImplementationDetails(throwable)),
                            null);
    }

    private static List<String> convertStackTraceAndRemoveImplementationDetails(Throwable throwable) {
        return ExceptionUtils.getThrowableList(throwable).stream()
                             .map(it -> removeImplementationDetails(ExceptionUtils.getStackTrace(it).split("\n")))
                             .flatMap(Collection::stream)
                             .collect(Collectors.toList());
    }

    private static List<String> removeImplementationDetails(String[] stackTrace) {
        return Stream.of(stackTrace)
                     .takeWhile(it -> !it.contains("jdk.internal.reflect.NativeMethodAccessorImpl.invoke0"))
                     .collect(Collectors.toList());
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
