package ru.ifmo.ctddev.isaev

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import ru.ifmo.ctddev.isaev.parser.LangLexer
import ru.ifmo.ctddev.isaev.parser.LangParser




/**
 * @author iisaev
 */
fun main(args: Array<String>) {
    val str = "2-3+4"
    interpret(str)
}

private fun interpret(program: String) {
    // Get our lexer
    val lexer = LangLexer(ANTLRInputStream(program))

    // Get a list of matched tokens
    val tokens = CommonTokenStream(lexer)

    // Pass the tokens to the parser
    val parser = LangParser(tokens)

    // Specify our entry point
    val programTree = parser.program();

    val f = false
    // Walk it and attach our listener
    /*val walker = ParseTreeWalker()
    val listener = Interpreter()
    walker.walk(listener, drinkSentenceContext)*/
}