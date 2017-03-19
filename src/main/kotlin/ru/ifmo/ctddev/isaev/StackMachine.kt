package ru.ifmo.ctddev.isaev

sealed class StackOp {
    class Read : StackOp()
    class Write : StackOp()
    class Nop : StackOp()
    class Push(val arg: Int) : StackOp()
    class Ld(val arg: String) : StackOp()
    class St(val arg: String) : StackOp()
    class Binop(val arg: String) : StackOp()
}

fun compile(node: Node): List<StackOp> {
    val result = ArrayList<StackOp>()
    compile(node, result)
    return result
}

fun compile(node: Node, stack: List<StackOp>) {
}

fun runStackMachine(operations: List<StackOp>) {
    val stack = ArrayList<Int>()
    operations.forEach {
        when (it) {
            is StackOp.Read -> {
            }
            is StackOp.Write -> {
            }
            is StackOp.Nop -> {
            }
            is StackOp.Push -> {
            }
            is StackOp.Ld -> {
            }
            is StackOp.St -> {
            }
            is StackOp.Binop -> {
            }
        }
    }
}