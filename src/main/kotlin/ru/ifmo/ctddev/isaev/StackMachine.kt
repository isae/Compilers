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
            return "COMM \"$comment\""
        }
    }

    class Call(val funName: String, val argsSize: Int) : StackOp() {
        override fun toString(): String {
            return "CALL $funName $argsSize"
        }
    }

    class Enter(val argNames: List<String>) : StackOp() {
        override fun toString(): String {
            return "ENTER $argNames"
        }
    }

    class Ret : StackOp() {
        override fun toString(): String {
            return "RET"
        }
    }
}

val random = Random()

fun getRandomLabel(): String {
    return "_${BigInteger(20, random).toString(32)}"
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
                else -> {
                    node.args.reversed().forEach { compile(it, stack) }
                    stack += StackOp.Comm("Call ${node.functionName}")
                    stack += StackOp.Call(node.functionName, node.args.size)
                }
            }
        }
        is AST.FunctionDef -> {
            stack += StackOp.Comm("Function '${node.functionName}' definition ...")
            stack += StackOp.Label("_${node.functionName}")
            stack += StackOp.Enter(node.argNames)
            //node.argNames.reversed().forEach { stack += StackOp.St(it) }
            stack += StackOp.Comm("Function '${node.functionName}' body ...")
            compile(node.body, stack)
            stack += StackOp.Ret()
        }
        is AST.Program -> {
            compile(node.functions, stack)
            stack += StackOp.Label("main")
            compile(node.statements, stack)
        }
        is AST.Conditional -> { //TODO: else is not mandatory
            val elifLabels = node.elifs.map { getRandomLabel() }
            stack += StackOp.Comm("If...")
            compile(node.expr, stack)
            val elseLabel = getRandomLabel()
            val afterIf = getRandomLabel()
            stack += if (elifLabels.isEmpty()) StackOp.Jif(elseLabel) else StackOp.Jif(elifLabels[0])
            stack += StackOp.Comm("Then...")
            compile(node.ifTrue, stack)
            stack += StackOp.Jump(afterIf)
            node.elifs.forEachIndexed { i, (expr, code) ->
                stack += StackOp.Label(elifLabels[i])
                stack += StackOp.Comm("Else If...")
                compile(expr, stack)
                stack += if (i == elifLabels.size - 1) StackOp.Jif(elseLabel) else StackOp.Jif(elifLabels[i + 1])
                stack += StackOp.Comm("Then...")
                compile(code, stack)
                stack += StackOp.Jump(afterIf)
            }
            stack += StackOp.Label(elseLabel)
            stack += StackOp.Comm("Else...")
            stack += compile(node.ifFalse)
            stack += StackOp.Label(afterIf)
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

fun runStackMachine(operations: List<StackOp>) {
    var funPrefix = "main"
    val mem = HashMap<String, Int>()

    val s = ArrayList<Int>()

    fun pop(): Int {
        val res = s.last()
        s.removeAt(s.size - 1)
        return res
    }

    fun push(arg: Int) {
        s += arg
    }

    val labels = HashMap<String, Int>()
    operations.forEachIndexed { i, op ->
        if (op is StackOp.Label) {
            if (labels[op.label] != null) {
                throw IllegalStateException("Duplicate label ${op.label}")
            }
            labels[op.label] = i
        }
    }
    var ip = labels["main"] ?: throw IllegalStateException("No such label: main")
    while (ip < operations.size) {
        val it = operations[ip]
        when (it) {
            is StackOp.Read -> push(builtInRead())
            is StackOp.Write -> builtInWrite(pop())
            is StackOp.Nop -> {
            }
            is StackOp.Label -> {
            }
            is StackOp.Comm -> {
                //println(it.comment)
            }
            is StackOp.Push -> push(it.arg)
            is StackOp.Ld -> {
                val value = mem["$funPrefix.${it.arg}"] ?: throw IllegalStateException("No such variable $it.arg")
                push(value)
            }
            is StackOp.St -> mem["$funPrefix.${it.arg}"] = pop()
            is StackOp.Binop -> {
                val right = pop()
                val left = pop()
                push(apply(left, right, it.op))
            }
            is StackOp.Jif -> {
                val condition = pop()
                if (condition == 0) {
                    ip = labels[it.label] ?: throw IllegalStateException("No such label ${it.label}")
                }
            }
            is StackOp.Jump -> {
                ip = labels[it.label] ?: throw IllegalStateException("No such label ${it.label}")
            }
            is StackOp.Call -> {
                push(ip) //pushing return address, arguments are already on stack
                val labelIp = labels["_${it.funName}"] ?: throw IllegalStateException("No such function '${it.funName}'")
                ip = labelIp
                funPrefix = "$funPrefix/${it.funName}"
            }
            is StackOp.Enter -> {
                val returnAddress = pop()
                it.argNames.forEach {
                    mem["$funPrefix.$it"] = pop()
                }
                push(returnAddress)
            }
            is StackOp.Ret -> {
                mem.entries.removeIf { it.key.startsWith(funPrefix) }
                val lastSlash = funPrefix.lastIndexOf('/')
                if (lastSlash != -1) {
                    funPrefix = funPrefix.substring(0, lastSlash)
                }
                val returnValue = pop()
                val returnAddress = pop()
                push(returnValue)
                ip = returnAddress
            }
        }
        ++ip
    }
}