package ru.ifmo.ctddev.isaev

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import ru.ifmo.ctddev.isaev.parser.LangLexer
import ru.ifmo.ctddev.isaev.parser.LangParser
import java.io.BufferedReader
import java.io.File
import java.io.PrintWriter


fun main(args: Array<String>) {
    val option = args[0]
    val fileName = args[1]
    val programText = File(fileName).readText()
    val program = buildAST(programText)
    when (option) {
        "-i" -> runInterpreter(program)
        "-s" -> runStackMachine(program)
        "-o" -> compileToASM(program)
    }
}

var read_count: Int = 0
var write_count: Int = 0

fun init() { //TODO: remove this shitty hack
    read_count = 0
    write_count = 0
}

fun printPrefix(): String {
    return if (write_count > 0) "" else ("> ".repeat(read_count))
}

fun builtInRead(reader: BufferedReader): Val {
    ++read_count
    val str = reader.readLine()
    try {
        return Val.Number(str!!.toInt())
    } catch (e: NumberFormatException) {
        return if (str.length == 1) {
            Val.Character(str[0])
        } else {
            Val.Str(str)
        }
    }
}

fun takeInt(arg: Val): Int {
    if (arg is Val.Number) {
        return arg.value
    } else {
        throw IllegalStateException("Type mismatch: expected Int, found ${arg::class.simpleName}")
    }
}

fun takeString(arg: Val): StringBuilder {
    if (arg is Val.Str) {
        return arg.value
    } else {
        throw IllegalStateException("Type mismatch: expected String, found ${arg::class.simpleName}")
    }
}

fun takeChar(arg: Val): Char {
    if (arg is Val.Character) {
        return arg.value
    } else {
        throw IllegalStateException("Type mismatch: expected Char, found ${arg::class.simpleName}")
    }
}

fun assertArgNumber(functionName: String, expected: Int, actual: Int) {
    if (expected != actual) {
        throw IllegalStateException("Expected $expected args for function $functionName, but found $actual")
    }
}

fun builtInWrite(arg: Val, writer: PrintWriter): Unit {
    val value = when (arg) {
        is Val.Number -> arg.value
        is Val.Str -> arg.value
        is Val.Character -> arg.value.toInt()
        else -> throw IllegalStateException("${arg::class.simpleName} has no value")
    }
    writer.println(printPrefix() + value)
    writer.flush()
    ++write_count
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

fun compileToASM(program: AST) {
    val compiledSTM = compile(program)
    val asmCode = compile(compiledSTM)
    asmCode.forEach(::println)
}