package ru.ifmo.ctddev.isaev

import java.util.*


sealed class AsmOp {
    class Nop : AsmOp() {
        override fun toString(): String {
            return "NOP"
        }
    }

    class Push(val arg: Int) : AsmOp() {
        override fun toString(): String {
            return "PUSH $arg"
        }
    }

    class Ld(val arg: String) : AsmOp() {
        override fun toString(): String {
            return "LD $arg"
        }
    }

    class St(val arg: String) : AsmOp() {
        override fun toString(): String {
            return "ST $arg"
        }
    }

    class Label(val label: String) : AsmOp() {

        override fun toString(): String {
            return "LABEL $label"
        }
    }

    class Jump(val label: String) : AsmOp() {
        override fun toString(): String {
            return "JUMP $label"
        }
    }

    class Jif(val label: String) : AsmOp() {
        override fun toString(): String {
            return "JIF $label"
        }
    }
}

fun compile(node: StackOp): List<AsmOp> {
    val result = ArrayList<AsmOp>()
    compile(node, result)
    return result
}

private fun compile(nodes: List<StackOp>): List<AsmOp> {
    val result = ArrayList<AsmOp>()
    compile(nodes, result)
    return result
}

private fun compile(nodes: List<StackOp>, stack: MutableList<AsmOp>) {
    nodes.forEach { compile(it, stack) }
}

private fun compile(node: StackOp, stack: MutableList<AsmOp>) {
    when (node) {
        is StackOp.Read -> {
        }
        is StackOp.Write -> {
        }
        is StackOp.Nop -> {
        }
        is StackOp.Label -> {
        }
        is StackOp.Comm -> {
        }
        is StackOp.Push -> {
        }
        is StackOp.Ld -> {
        }
        is StackOp.St -> {
        }
        is StackOp.Binop -> {
        }
        is StackOp.Jif -> {
        }
        is StackOp.Jump -> {
        }
        is StackOp.Call -> {
        }
        is StackOp.Enter -> {
        }
        is StackOp.Ret -> {
        }
    }
}