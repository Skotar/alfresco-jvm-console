package pl.skotar.alfresco.module.jvmconsole.internal;

class ClassByteCodeClassLoader extends ClassLoader {

    private final byte[] byteCode;
    private final String canonicalClassName;

    ClassByteCodeClassLoader(ClassLoader classLoader, byte[] byteCode, String canonicalClassName) {
        super(classLoader);
        this.byteCode = byteCode;
        this.canonicalClassName = canonicalClassName;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.equals(canonicalClassName)) {
            return defineClass(canonicalClassName, byteCode, 0, byteCode.length);
        } else {
            return super.findClass(name);
        }
    }
}
