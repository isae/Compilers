package ru.ifmo.ctddev.isaev

import org.junit.Assert
import org.junit.Test
import java.io.*

/**
 * @author iisaev
 */
class CoreTest {
    @Test
    fun testInterpreter() {
        File("./compiler-tests/core")
                .listFiles()
                .filter { it.name.startsWith("test0") }
                .map { it.nameWithoutExtension }
                .distinct()
                .forEach { testInterpreterOnFile(it) }
    }

    @Test
    fun testStm() {
        File("./compiler-tests/core")
                .listFiles()
                .filter { it.name.startsWith("test0") }
                .map { it.nameWithoutExtension }
                .distinct()
                .forEach { testStmOnFile(it) }
    }

    fun testInterpreterOnFile(fileName: String) {
        val expr = File("./compiler-tests/core/$fileName.expr").readText()
        val input = File("./compiler-tests/core/$fileName.input").readText()
        val expectedOutput = File("./compiler-tests/core/orig/$fileName.log").readText()
        println(expr)
        println(input)
        println(expectedOutput)
        val compiledProgram = buildAST(expr)
        val inputReader = BufferedReader(StringReader(input))
        val outputStream = ByteArrayOutputStream(256)
        val outputWriter = PrintWriter(OutputStreamWriter(outputStream))
        Interpreter(inputReader, outputWriter).run(compiledProgram)
        val actualOutput = String(outputStream.toByteArray())
        Assert.assertEquals("Expected different output", expectedOutput, actualOutput)
    }

    fun testStmOnFile(fileName: String) {
        val expr = File("./compiler-tests/core/$fileName.expr").readText()
        val input = File("./compiler-tests/core/$fileName.input").readText()
        val expectedOutput = File("./compiler-tests/core/orig/$fileName.log").readText()
        println(expr)
        println(input)
        println(expectedOutput)
        val programAST = buildAST(expr)
        val compiledSTM = compile(programAST)
        val inputReader = BufferedReader(StringReader(input))
        val outputStream = ByteArrayOutputStream(256)
        val outputWriter = PrintWriter(OutputStreamWriter(outputStream))
        StackMachine(inputReader, outputWriter).run(compiledSTM)
        val actualOutput = String(outputStream.toByteArray())
        Assert.assertEquals("Expected different output", expectedOutput, actualOutput)
    }
}