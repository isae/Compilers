package ru.ifmo.ctddev.isaev

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import ru.ifmo.ctddev.isaev.parser.LangLexer
import ru.ifmo.ctddev.isaev.parser.LangParser
import java.io.File
import java.io.PrintWriter


fun main(args: Array<String>) {
    val option = args[0]
    val fileName = args[1]
    val file = File(fileName)
    val programText = file.readText()
    val program = buildAST(programText)
    when (option) {
        "-i" -> runInterpreter(program)
        "-s" -> runStackMachine(program)
        "-o" -> compileToASM(program, file.absoluteFile)
    }
}

fun buildAST(program: String): AST {
    val lexer = LangLexer(ANTLRInputStream(program))
    val tokens = CommonTokenStream(lexer)
    val parser = LangParser(tokens)
    val programTree = parser.program()
    return ASTBuilder().visitProgram(programTree)
}

fun runInterpreter(program: AST) {
    Interpreter().run(program)
}

fun runStackMachine(program: AST) {
    val compiledSTM = compile(program)
    compiledSTM.forEachIndexed { i, op -> println("$i: $op") }
    StackMachine().run(compiledSTM)
}

fun compileToASM(program: AST, file: File) {
    val compiledSTM = compile(program)
    val asmCode = compile(compiledSTM)
    println("File to compile: ${file.absolutePath}")
    println("Parent directory: ${file.parentFile.absolutePath}")
    PrintWriter("${file.parentFile.absolutePath}/out/${file.name}.asm").use { writer ->
        asmCode.forEach { writer.println(it)}
    }
}