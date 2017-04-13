package ru.ifmo.ctddev.isaev

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
        val compiledSTM = compile(program)
        println("Byte code:")
        compiledSTM.forEachIndexed { i, op -> println("$i: $op") }
        println("ASM code:")
        val asmCode = compile(compiledSTM)
        asmCode.forEach(::println)
    }
}