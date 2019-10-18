package pl.skotar.alfresco.module.jvmconsole.internal

import io.kotlintest.matchers.instanceOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.junit.Test
import pl.skotar.alfresco.module.jvmconsole.internal.Executor.ClassByteCode
import pl.skotar.alfresco.module.jvmconsole.internal.util.getResourceAsBytes
import java.lang.reflect.InvocationTargetException

class ExecutorKotlinTest {

    companion object {
        private val testClassByteCodes = listOf(
            ClassByteCode("pl.skotar.alfresco.module.jvmconsole.test.TestClassKotlin", getResourceAsBytes("/TestClassKotlin")),
            ClassByteCode("pl.skotar.alfresco.module.jvmconsole.test.TestClassKotlin\$Nested", getResourceAsBytes("/TestClassKotlin\$Nested")),
            ClassByteCode(
                "pl.skotar.alfresco.module.jvmconsole.test.TestClassKotlin\$executeLambdaAndReturnString\$1",
                getResourceAsBytes("/TestClassKotlin\$executeLambdaAndReturnString\$1")
            )
        )
        private const val className = "pl.skotar.alfresco.module.jvmconsole.test.TestClassKotlin"

        private val constructorWithParametersTestClassByteCodes = listOf(
            ClassByteCode(
                "pl.skotar.alfresco.module.jvmconsole.test.ConstructorWithParametersTestClassKotlin",
                getResourceAsBytes("/ConstructorWithParametersTestClassKotlin")
            )
        )
        private const val classWithParametersTestName = "pl.skotar.alfresco.module.jvmconsole.test.ConstructorWithParametersTestClassKotlin"
    }

    @Test
    fun execute() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "execute")
    }

    @Test
    fun executeAndReturnStringList() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "executeAndReturnStringList") shouldBe
                listOf("test")
    }

    @Test
    fun executeAndReturnIntegerList() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "executeAndReturnIntegerList") shouldBe
                listOf("-1")
    }

    @Test
    fun executeAndReturnString() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "executeAndReturnString") shouldBe
                listOf("test")
    }

    @Test
    fun executeNestedAndReturnString() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "executeNestedAndReturnString") shouldBe
                listOf("test")
    }

    @Test
    fun executeLambdaAndReturnString() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCodes, className, "executeLambdaAndReturnString") shouldBe
                listOf("test")
    }

    @Test
    fun `executeWithParameter _ should throw IllegalStateException because function contains parameters`() {
        shouldThrow<IllegalStateException> {
            Executor(mockk(relaxed = true))
                .execute(testClassByteCodes, className, "executeWithParameter")
        }.message shouldBe "Class <${testClassByteCodes.first().canonicalClassName}> doesn't contain no-argument <executeWithParameter> function"
    }

    @Test
    fun `executeAndThrowException _ should throw InvocationTargetException that contains IllegalStateException`() {
        shouldThrow<InvocationTargetException> {
            Executor(mockk(relaxed = true))
                .execute(testClassByteCodes, className, "executeAndThrowException")
        }.let {
            it.targetException shouldBe instanceOf(IllegalStateException::class)
            it.targetException.message shouldBe "I'm in incorrect state"
        }
    }

    @Test
    fun `noMatter _ should throw IllegalStateException because it contains constructor with parameters`() {
        shouldThrow<IllegalStateException> {
            Executor(mockk(relaxed = true))
                .execute(constructorWithParametersTestClassByteCodes, classWithParametersTestName, "noMatter")
        }.message shouldBe "Class <${constructorWithParametersTestClassByteCodes.first().canonicalClassName}> doesn't contain no-argument constructor"
    }
}