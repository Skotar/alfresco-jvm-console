package pl.skotar.alfresco.module.jvmconsole.internal

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.junit.Test
import pl.skotar.alfresco.module.jvmconsole.internal.util.getResourceAsBytes

class ExecutorKotlinTest {

    companion object {
        private val testClassByteCode = getResourceAsBytes("/TestClassKotlin")
        private val constructorWithParametersTestClassByteCode = getResourceAsBytes("/ConstructorWithParametersTestClassKotlin")

        private const val className = "pl.skotar.alfresco.module.jvmconsole.test.TestClassKotlin"
        private const val classWithParametersTestName = "pl.skotar.alfresco.module.jvmconsole.test.ConstructorWithParametersTestClassKotlin"
    }

    @Test
    fun execute() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCode, className, "execute")
    }

    @Test
    fun executeAndReturnStringList() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCode, className, "executeAndReturnStringList") shouldBe
                listOf("test")
    }

    @Test
    fun executeAndReturnIntegerList() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCode, className, "executeAndReturnIntegerList") shouldBe
                listOf("-1")
    }

    @Test
    fun executeAndReturnString() {
        Executor(mockk(relaxed = true))
            .execute(testClassByteCode, className, "executeAndReturnString") shouldBe
                listOf("test")
    }

    @Test
    fun `executeWithParameter _ should throw IllegalStateException because function contains parameters`() {
        shouldThrow<IllegalStateException> {
            Executor(mockk(relaxed = true))
                .execute(testClassByteCode, className, "executeWithParameter")
        }.message shouldBe "Class <$className> doesn't contain no-argument <executeWithParameter> function"
    }

    @Test
    fun `executeAndThrowException _ should throw IllegalStateException`() {
        shouldThrow<IllegalStateException> {
            Executor(mockk(relaxed = true))
                .execute(testClassByteCode, className, "executeAndThrowException")
        }.message shouldBe "I'm in incorrect state"
    }

    @Test
    fun `noMatter _ should throw IllegalStateException because it contains constructor with parameters`() {
        shouldThrow<IllegalStateException> {
            Executor(mockk(relaxed = true))
                .execute(constructorWithParametersTestClassByteCode, classWithParametersTestName, "noMatter")
        }.message shouldBe "Class <$classWithParametersTestName> doesn't contain no-argument constructor"
    }
}