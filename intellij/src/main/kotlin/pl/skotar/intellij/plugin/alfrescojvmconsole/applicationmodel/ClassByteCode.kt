package pl.skotar.intellij.plugin.alfrescojvmconsole.applicationmodel

internal data class ClassByteCode(
    val canonicalClassName: String,
    val byteArray: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClassByteCode) return false

        if (canonicalClassName != other.canonicalClassName) return false
        if (!byteArray.contentEquals(other.byteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = canonicalClassName.hashCode()
        result = 31 * result + byteArray.contentHashCode()
        return result
    }
}