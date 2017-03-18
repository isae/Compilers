package ru.ifmo.ctddev.isaev

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import ru.ifmo.ctddev.isaev.parser.LangLexer
import ru.ifmo.ctddev.isaev.parser.LangParser
import java.io.File


/**
 * @author iisaev
 */
fun main(args: Array<String>) {
    while (true) {
        val testName = readLine()
        val programText = File("./compiler-tests/core/${testName}.expr").readText()
        println(programText)
        interpret(programText)
    }
}

private fun interpret(program: String) {
    val lexer = LangLexer(ANTLRInputStream(program))
    val tokens = CommonTokenStream(lexer)
    val parser = LangParser(tokens)
    val programTree = parser.program()
    val rootNode = ASTBuilder().visitProgram(programTree)
    rootNode.interpret()
}