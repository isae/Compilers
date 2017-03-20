package ru.ifmo.ctddev.isaev

import java.math.BigInteger
import java.util.*
import kotlin.collections.HashMap


sealed class StackOp {
    class Read : StackOp() {
        override fun toString(): String {
            return "READ"
        }
    }

    class Write : StackOp() {
        override fun toString(): String {
            return "WRITE"
        }
    }

    class Nop : StackOp() {
        override fun toString(): String {
            return "NOP"
        }
    }

    class Push(val arg: Int) : StackOp() {
        override fun toString(): String {
            return "PUSH $arg"
        }
    }

    class Ld(val arg: String) : StackOp() {
        override fun toString(): String {
            return "LD $arg"
        }
    }

    class St(val arg: String) : StackOp() {
        override fun toString(): String {
            return "ST $arg"
        }
    }

    class Binop(val op: String) : StackOp() {
        override fun toString(): String {
            return "BINOP $op"
        }
    }

    class Label(val label: String) : StackOp() {
        constructor() : this(getRandomLabel())

        override fun toString(): String {
            return "LABEL $label"
        }
    }

    class Jump(val label: String) : StackOp() {
        override fun toString(): String {
            return "JUMP $label"
        }
    }

    class Jif(val label: String) : StackOp() {
        override fun toString(): String {
            return "JIF $label"
        }
    }

    class Comm(val comment: String) : StackOp() {
        override fun toString(): String {
            return "COMM $comment"
        }
    }
}

val random = Random()

fun getRandomLabel(): String {
    return BigInteger(20, random).toString(32)
}

fun compile(node: AST): List<StackOp> {
    val result = ArrayList<StackOp>()
    compile(node, result)
    return result
}

private fun compile(nodes: List<AST>): List<StackOp> {
    val result = ArrayList<StackOp>()
    compile(nodes, result)
    return result
}

private fun compile(nodes: List<AST>, stack: MutableList<StackOp>) {
    nodes.forEach { compile(it, stack) }
}

private fun compile(node: AST, stack: MutableList<StackOp>) {
    when (node) {
        is AST.Skip -> {
        }
        is AST.Const -> stack += StackOp.Push(node.number)
        is AST.Variable -> stack += StackOp.Ld(node.name)
        is AST.UnaryMinus -> { // -a == 0-a
            stack += StackOp.Push(0)
            compile(node.arg, stack)
            stack += StackOp.Binop("-")
        }
        is AST.Binary -> {
            compile(node.left, stack)
            compile(node.right, stack)
            stack += StackOp.Binop(node.op)
        }
        is AST.FunctionCall -> {
            when (node.functionName) {
                "read" -> stack += StackOp.Read()
                "write" -> {
                    if (node.args.size != 1) {
                        TODO("Vararg write")
                    }
                    compile(node.args[0], stack)
                    stack += StackOp.Write()
                }
                else -> TODO("Function calls")
            }
        }
        is AST.FunctionDef -> TODO("Function definitions")
        is AST.Program -> node.statements.forEach { compile(it, stack) }
        is AST.Conditional -> {
            if (node.elifs.isNotEmpty()) {
                TODO("elif statements")
            }
            stack += StackOp.Comm("If...")
            compile(node.expr, stack)
            val label = getRandomLabel()
            stack += StackOp.Jif(label)
            stack += StackOp.Comm("Then...")
            compile(node.ifTrue, stack)
            stack += StackOp.Label(label)
            stack += StackOp.Comm("Else...")
            stack += compile(node.ifFalse)
            stack += StackOp.Comm("EndIf")
        }
        is AST.Assignment -> {
            compile(node.toAssign, stack)
            stack += StackOp.St(node.variable.name)
        }
        is AST.WhileLoop -> {
            val startLabel = getRandomLabel()
            val endLabel = getRandomLabel()
            stack += StackOp.Label(startLabel)
            stack += StackOp.Comm("While...")
            compile(node.expr, stack)
            stack += StackOp.Jif(endLabel)
            stack += StackOp.Comm("Do...")
            compile(node.loop, stack)
            stack += StackOp.Comm("EndWhile")
            stack += StackOp.Jump(startLabel)
            stack += StackOp.Label(endLabel)
        }
        is AST.ForLoop -> {
            compile(node.init, stack)
            val startLabel = getRandomLabel()
            val endLabel = getRandomLabel()
            stack += StackOp.Label(startLabel)
            stack += StackOp.Comm("For...")
            compile(node.expr, stack)
            stack += StackOp.Jif(endLabel)
            stack += StackOp.Comm("Do...")
            compile(node.loop, stack)
            stack += StackOp.Comm("Increment...")
            compile(node.increment, stack)
            stack += StackOp.Comm("EndFor")
            stack += StackOp.Jump(startLabel)
            stack += StackOp.Label(endLabel)
        }
        is AST.RepeatLoop -> {
            val startLabel = getRandomLabel()
            stack += StackOp.Label(startLabel)
            stack += StackOp.Comm("Repeat...")
            compile(node.loop, stack)
            stack += StackOp.Comm("Until...")
            compile(node.expr, stack) // we have zero if condition is unsuccessful
            stack += StackOp.Comm("EndRepeat")
            stack += StackOp.Jif(startLabel)
        }
    }
}

fun MutableList<Int>.push(arg: Int) {
    this += arg
}

fun MutableList<Int>.pop(): Int {
    val res = this.last()
    removeAt(this.size - 1)
    return res
}

fun runStackMachine(operations: List<StackOp>) {
    val s = ArrayList<Int>()
    val mem = HashMap<String, Int>()
    val labels = HashMap<String, Int>()
    operations.forEachIndexed { i, op ->
        if (op is StackOp.Label) {
            if (labels[op.label] != null) {
                throw IllegalStateException("Duplicate label ${op.label}")
            }
            labels[op.label] = i
        }
    }
    var ip = 0
    while (ip < operations.size) {
        val it = operations[ip]
        when (it) {
            is StackOp.Read -> s.push(readLine()!!.toInt())
            is StackOp.Write -> println(s.pop())
            is StackOp.Nop -> {
            }
            is StackOp.Comm -> {
                println(it.comment)
            }
            is StackOp.Push -> s.push(it.arg)
            is StackOp.Ld -> {
                val value = mem[it.arg] ?: throw IllegalStateException("No such variable $it.arg")
                s.push(value)
            }
            is StackOp.St -> mem[it.arg] = s.pop()
            is StackOp.Binop -> {
                val right = s.pop()
                val left = s.pop()
                s.push(apply(left, right, it.op))
            }
            is StackOp.Jif -> {
                val condition = s.pop()
                if (condition == 0) {
                    ip = labels[it.label] ?: throw IllegalStateException("No such label ${it.label}")
                }
            }
            is StackOp.Jump -> {
                ip = labels[it.label] ?: throw IllegalStateException("No such label ${it.label}")
            }
        }
        ++ip
    }
}