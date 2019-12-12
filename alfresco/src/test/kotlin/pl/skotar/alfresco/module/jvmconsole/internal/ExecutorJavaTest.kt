package pl.skotar.alfresco.module.jvmconsole.internal

import io.kotlintest.matchers.instanceOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactly
import io.mockk.mockk
import org.junit.Test
import pl.skotar.alfresco.module.jvmconsole.internal.Executor.ClassByteCode
import pl.skotar.alfresco.module.jvmconsole.internal.util.getResourceAsBytes
import java.lang.reflect.InvocationTargetException

class ExecutorJavaTest {

    companion object {
        private val testClassByteCodes = listOf(
            ClassByteCode("pl.skotar.alfresco.module.jvmconsole.test.TestClassJava", getResourceAsBytes("/TestClassJava")),
            ClassByteCode("pl.skotar.alfresco.module.jvmconsole.test.TestClassJava\$Nested", getResourceAsBytes("/TestClassJava\$Nested"))
        )
        private const val className = "pl.skotar.alfresco.module.jvmconsole.test.TestClassJava"

        private val constructorWithParametersTestClassByteCodes = listOf(
            ClassByteCode(
                "pl.skotar.alfresco.module.jvmconsole.test.ConstructorWithParametersTestClassJava",
                getResourceAsBytes("/ConstructorWithParametersTestClassJava")
            )
        )
        private const val classWithParametersTestName = "pl.skotar.alfresco.module.jvmconsole.test.ConstructorWithParametersTestClassJava"
    }

    @Test
    fun execute() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "execute", false)
    }

    @Test
    fun `execute _ return string list`() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "executeAndReturnStringList", false) shouldBe listOf("test")
    }

    @Test
    fun `execute _ return integer list`() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "executeAndReturnIntegerList", false) shouldBe listOf("-1")
    }

    @Test
    fun `execute _ return string`() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "executeAndReturnString", false) shouldBe listOf("test")
    }

    @Test
    fun `executeNested _ return string`() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "executeNestedAndReturnString", false) shouldBe listOf("test")
    }

    @Test
    fun `executeLambda _ return string`() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "executeLambdaAndReturnString", false) shouldBe listOf("test")
    }

    @Test
    fun `execute _ useMainClassLoader`() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "execute", true)

        shouldThrowExactly<IllegalArgumentException> {
            Executor(mockk(relaxed = true))
                .execute(testClassByteCodes, className, "execute", true)
        }.message shouldBe "Class is already loaded in class loader. Change name"
    }

    @Test
    fun `executeWithParameter _ should throw IllegalArgumentException because function contains parameters`() {
        shouldThrowExactly<IllegalArgumentException> {
            Executor(mockk(relaxed = true))
                .execute(testClassByteCodes, className, "executeWithParameter", false)
        }.message shouldBe "Class <${testClassByteCodes.first().canonicalClassName}> doesn't contain no-argument <executeWithParameter> function"
    }

    @Test
    fun `executeAndThrowException _ should throw InvocationTargetException that contains IllegalStateException`() {
        shouldThrowExactly<InvocationTargetException> {
            Executor(mockk(relaxed = true))
                .execute(testClassByteCodes, className, "executeAndThrowException", false)
        }.let {
            it.targetException shouldBe instanceOf(IllegalStateException::class)
            it.targetException.message shouldBe "I'm in incorrect state"
        }
    }

    @Test
    fun `noMatter _ should throw IllegalArgumentException because it contains constructor with parameters`() {
        shouldThrowExactly<IllegalArgumentException> {
            Executor(mockk(relaxed = true))
                .execute(constructorWithParametersTestClassByteCodes, classWithParametersTestName, "noMatter", false)
        }.message shouldBe "Class <${constructorWithParametersTestClassByteCodes.first().canonicalClassName}> doesn't contain no-argument constructor"
    }
}