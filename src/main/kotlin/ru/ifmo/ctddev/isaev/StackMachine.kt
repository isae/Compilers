package ru.ifmo.ctddev.isaev

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

    class Jump(val pos: Int) : StackOp() {
        override fun toString(): String {
            return "JUMP $pos"
        }
    }
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
            compile(node.expr, stack)
            val ifTrue = compile(node.ifTrue)
            val ifTrueSize = ifTrue.size + 1 // +1 for jump  
            stack += StackOp.Jump(ifTrueSize)
            stack += ifTrue
            stack += compile(node.ifFalse)
        }
        is AST.Assignment -> {
            compile(node.toAssign, stack)
            stack += StackOp.St(node.variable.name)
        }
        is AST.WhileLoop -> {
            val expr = compile(node.expr)
            val loop = compile(node.loop)
            val frontJumpPos = loop.size + 3 // +3 for front and back jump (2 ops)   
            stack += expr
            stack += StackOp.Jump(frontJumpPos)
            stack += loop
            stack += StackOp.Push(0)
            stack += StackOp.Jump(-(expr.size + loop.size + 2))
        }
        is AST.ForLoop -> TODO("For loops")
        is AST.RepeatLoop -> {
            val pos = stack.size
            node.loop.forEach { compile(it, stack) }
            compile(node.expr, stack) // we have zero if condition is unsuccessful
            stack += StackOp.Jump(pos - stack.size)
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

    var ip = 0
    while (ip < operations.size) {
        val it = operations[ip]
        when (it) {
            is StackOp.Read -> s.push(readLine()!!.toInt())
            is StackOp.Write -> println(s.pop())
            is StackOp.Nop -> {
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
            is StackOp.Jump -> {
                val condition = s.pop()
                if (condition == 0) {
                    ip += it.pos - 1
                }
            }
        }
        ++ip
    }
}