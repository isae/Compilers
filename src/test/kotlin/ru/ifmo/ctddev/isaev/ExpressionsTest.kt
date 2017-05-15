package ru.ifmo.ctddev.isaev

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.*


/**
 * @author iisaev
 */

@RunWith(Parameterized::class)
class ExpressionsTest(val testName: String,
                      val expr: String,
                      val input: String,
                      val expectedOutput: String) {

    companion object {
        @JvmStatic
        val testFolder = "./compiler-tests/expressions"

        @JvmStatic
        @Parameterized.Parameters
        fun getParameters(): List<Array<String>> {
            return File(testFolder)
                    .listFiles()
                    .filter { it.name.startsWith("generated0") }
                    .map { it.nameWithoutExtension }
                    .distinct()
                    .filter { fileName ->
                        File("${ExpressionsTest.testFolder}/$fileName.expr").exists() &&
                                File("${ExpressionsTest.testFolder}/$fileName.input").exists() &&
                                File("${ExpressionsTest.testFolder}/orig/$fileName.log").exists()
                    }
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
        val compiledProgram = buildAST(expr)
        val inputReader = BufferedReader(StringReader(input))
        val outputStream = ByteArrayOutputStream(256)
        val outputWriter = PrintWriter(OutputStreamWriter(outputStream))
        Interpreter(inputReader, outputWriter).run(compiledProgram)
        val actualOutput = String(outputStream.toByteArray())
        assertEquals("$testName: Expected different output for program\n\n$expr\n\nand input\n\n$input\n\n", 
                expectedOutput, actualOutput)
    }

    @Test
    fun testStm() {
        val programAST = buildAST(expr)
        val compiledSTM = compile(programAST)
        val inputReader = BufferedReader(StringReader(input))
        val outputStream = ByteArrayOutputStream(256)
        val outputWriter = PrintWriter(OutputStreamWriter(outputStream))
        StackMachine(inputReader, outputWriter).run(compiledSTM)
        val actualOutput = String(outputStream.toByteArray())
        assertEquals("$testName: Expected different output for program\n\n$expr\n\nand input\n\n$input\n\n",
                expectedOutput, actualOutput)
    }
}