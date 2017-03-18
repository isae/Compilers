package ru.ifmo.ctddev.isaev

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import ru.ifmo.ctddev.isaev.parser.LangLexer
import ru.ifmo.ctddev.isaev.parser.LangParser




/**
 * @author iisaev
 */
fun main(args: Array<String>) {
    val str = """x := read();
y := read();
z := x*y*3;
write (z)"""
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
    val programTree = parser.program()
    
    val astBuilder = ASTBuilder()
    
    val rootNode = astBuilder.visitProgram(programTree)

    val f = false
    // Walk it and attach our listener
    /*val walker = ParseTreeWalker()
    val listener = ASTBuilder()
    walker.walk(listener, drinkSentenceContext)*/
}