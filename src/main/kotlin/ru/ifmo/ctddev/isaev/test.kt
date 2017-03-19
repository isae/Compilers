package ru.ifmo.ctddev.isaev

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import ru.ifmo.ctddev.isaev.parser.LangLexer
import ru.ifmo.ctddev.isaev.parser.LangParser
import java.io.File


fun main(args: Array<String>) {
    while (true) {
        val testName = readLine()
        val programText = File("./compiler-tests/core/test0${testName}.expr").readText()
        val input = File("./compiler-tests/core/test0${testName}.input").readText()
        val output = File("./compiler-tests/core/orig/test0${testName}.log").readText()
        println(programText)
        println("Input: $input")
        println("Output: $output")
        val program = buildAST(programText)
        runStackMachine(program)
    }
}

fun buildAST(program: String): AST {
    val lexer = LangLexer(ANTLRInputStream(program))
    val tokens = CommonTokenStream(lexer)
    val parser = LangParser(tokens)
    val programTree = parser.program()
    return ASTBuilder().visitProgram(programTree)
}

private fun runInterpreter(program: AST) {
    interpret(program)
}

private fun runStackMachine(program: AST) {
    val compiledSTM = compileSTM(program)
    compiledSTM.forEach(::println)
    runStackMachine(compiledSTM)
}