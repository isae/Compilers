package ru.ifmo.ctddev.isaev

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.*


/**
 * @author iisaev
 */

@RunWith(Parameterized::class)
class CoreTest(val testName: String,
               val expr: String,
               val input: String,
               val expectedOutput: String) {
    
    companion object {
        @JvmStatic
        val testFolder = "./compiler-tests/core"
        
        @JvmStatic
        @Parameterized.Parameters
        fun getParameters(): List<Array<String>> {
            return File(testFolder)
                    .listFiles()
                    .filter { it.name.startsWith("test0") }
                    .map { it.nameWithoutExtension }
                    .distinct()
                    .map { fileName ->
                        val expr = File("$testFolder/$fileName.expr").readText()
                        val input = File("$testFolder/$fileName.input").readText()
                        val expectedOutput = File("$testFolder/orig/$fileName.log").readText()
                        arrayOf(fileName, expr, input, expectedOutput)
                    }
        }
    }

    @Test
    fun testInterpreter() {
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

    @Test
    fun testStm() {
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
        Assert.assertEquals("$testName: expected different output", expectedOutput, actualOutput)
    }
}