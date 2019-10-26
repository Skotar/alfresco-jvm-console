package pl.skotar.alfresco.module.jvmconsole.internal;

import pl.skotar.alfresco.module.jvmconsole.internal.Executor.ClassByteCode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.Arrays;
import java.util.List;

class ReflectionUtils {

    private ReflectionUtils() {
    }

    static ClassByteCodeClassLoader createClassLoader(ClassLoader parent, List<ClassByteCode> classByteCodes) {
        return new ClassByteCodeClassLoader(parent, classByteCodes);
    }

    static ClassLoader addClassesToCurrentClassLoader(ClassLoader parent, List<ClassByteCode> classByteCodes) throws ReflectiveOperationException {
        Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        try {
            defineClassMethod.setAccessible(true);
            for (ClassByteCode classByteCode : classByteCodes) {
                defineClassMethod.invoke(parent, classByteCode.canonicalClassName, classByteCode.byteCode, 0, classByteCode.byteCode.length);
            }
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof LinkageError) {
                throw new IllegalArgumentException("Class is already loaded in class loader. Change name");
            } else {
                throw e;
            }
        } finally {
            defineClassMethod.setAccessible(false);
        }

        return parent;
    }

    static Class<?> getClass(ClassLoader classLoader, String canonicalClassName) throws ClassNotFoundException {
        return classLoader.loadClass(canonicalClassName);
    }

    static Object createInstanceUsingNoArgumentConstructor(Class<?> clazz) throws ReflectiveOperationException {
        return Arrays.stream(clazz.getConstructors())
                     .filter(constructor -> constructor.getParameterCount() == 0)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Class <" + clazz.getCanonicalName() + "> doesn't contain no-argument constructor"))
                     .newInstance();
    }

    static Object invokeNoArgumentFunction(Object instance, String name) throws InvocationTargetException, IllegalAccessException {
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
                         .orElseThrow(() -> new IllegalArgumentException("Class <" + clazz.getCanonicalName() + "> doesn't contain no-argument <" + name + "> function"))
                         .invoke(instance);
        } finally {
            System.setSecurityManager(defaultSecurityManager);
        }
    }

    private static boolean validateMethod(Method method, String name) {
        return method.getName().equals(name) && method.getParameterCount() == 0;
    }
}
