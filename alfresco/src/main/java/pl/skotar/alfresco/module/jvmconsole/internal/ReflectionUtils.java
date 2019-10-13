package pl.skotar.alfresco.module.jvmconsole.internal;

import java.lang.reflect.Method;
import java.security.Permission;
import java.util.Arrays;

class ReflectionUtils {

    private ReflectionUtils() {
    }

    static Class<?> loadClass(ClassLoader parent, byte[] byteCode, String canonicalClassName) throws ClassNotFoundException {
        return new ClassByteCodeClassLoader(parent, byteCode, canonicalClassName)
                .loadClass(canonicalClassName);
    }

    static Object createInstanceUsingNoArgumentConstructor(Class<?> clazz) throws ReflectiveOperationException {
        return Arrays.stream(clazz.getConstructors())
                     .filter(constructor -> constructor.getParameterCount() == 0)
                     .findFirst()
                     .orElseThrow(() -> new IllegalStateException("Class <" + clazz.getCanonicalName() + "> doesn't contain no-argument constructor"))
                     .newInstance();
    }

    static Object invokeNoArgumentFunction(Object instance, String name) throws Throwable {
        SecurityManager defaultSecurityManager = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                // deliberately omitted. Permit everything
            }
        });

        try {
            Class<?> clazz = instance.getClass();
            return Arrays.stream(clazz.getMethods())
                         .filter(method -> validateMethod(method, name))
                         .findFirst()
                         .orElseThrow(() -> new IllegalStateException("Class <" + clazz.getCanonicalName() + "> doesn't contain no-argument <" + name + "> function"))
                         .invoke(instance);
        } finally {
            System.setSecurityManager(defaultSecurityManager);
        }
    }

    private static boolean validateMethod(Method method, String name) {
        return method.getName().equals(name) && method.getParameterCount() == 0;
    }
}
