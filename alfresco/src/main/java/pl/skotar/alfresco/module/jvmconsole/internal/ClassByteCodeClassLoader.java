package pl.skotar.alfresco.module.jvmconsole.internal;

import pl.skotar.alfresco.module.jvmconsole.internal.Executor.ClassByteCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ClassByteCodeClassLoader extends ClassLoader {

    private final Map<String, byte[]> canonicalClassNameToByteCodeMap;

    ClassByteCodeClassLoader(ClassLoader classLoader, List<ClassByteCode> classByteCodes) {
        super(classLoader);
        this.canonicalClassNameToByteCodeMap = convertToMap(classByteCodes);
    }

    private Map<String, byte[]> convertToMap(List<ClassByteCode> classByteCodes) {
        return classByteCodes.stream()
                             .collect(Collectors.toMap(it -> it.canonicalClassName, it -> it.byteCode));
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] byteCode = canonicalClassNameToByteCodeMap.get(name);
        if (byteCode != null) {
            return defineClass(name, byteCode, 0, byteCode.length);
        } else {
            return super.findClass(name);
        }
    }
}
