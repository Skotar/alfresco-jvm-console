package pl.skotar.alfresco.module.jvmconsole.internal;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Executor {

    private final BeanInjector beanInjector;

    public Executor(AutowireCapableBeanFactory autowireCapableBeanFactory) {
        this.beanInjector = new BeanInjector(autowireCapableBeanFactory);
    }

    public List<String> execute(List<ClassByteCode> classByteCodes,
                                String canonicalClassName,
                                String functionName) throws ReflectiveOperationException {
        Class<?> clazz = ReflectionUtils.loadClass(this.getClass().getClassLoader(), classByteCodes, canonicalClassName);
        Object instance = ReflectionUtils.createInstanceUsingNoArgumentConstructor(clazz);
        beanInjector.inject(instance);
        Object result = ReflectionUtils.invokeNoArgumentFunction(instance, functionName);
        return processMessages(result);
    }

    @SuppressWarnings("unchecked")
    private List<String> processMessages(Object result) {
        if (result == null) {
            return Collections.emptyList();
        } else if (result instanceof List) {
            return ((List<Object>) result).stream()
                                          .map(Object::toString)
                                          .collect(Collectors.toList());
        } else {
            return Collections.singletonList(result.toString());
        }
    }

    public static class ClassByteCode {

        final String canonicalClassName;
        final byte[] byteCode;

        public ClassByteCode(String canonicalClassName, byte[] byteCode) {
            this.canonicalClassName = canonicalClassName;
            this.byteCode = byteCode;
        }
    }
}
