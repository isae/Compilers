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

public fun compileSTM(node: AST): List<StackOp> {
    val result = ArrayList<StackOp>()
    compile(node, result)
    return result
}

private fun compile(node: AST, stack: MutableList<StackOp>) {
    when (node) {
        is AST.Skip -> stack += StackOp.Nop()
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
        is AST.Dand -> {
            TODO("&& is not supported yet")
        }
        is AST.Dor -> {
            TODO("|| is not supported yet")
        }
        is AST.FunctionCall -> {
            when (node.functionName) {
                "read" -> stack += StackOp.Read()
                "write" -> {
                    if (node.args.size != 1) {
                        TODO("Vararg write is not supported yet")
                    }
                    compile(node.args[0], stack)
                    stack += StackOp.Write()
                }
                else -> TODO("Function calls are not supported yet")
            }
        }
        is AST.FunctionDef -> TODO("Function definitions are not supported yet")
        is AST.Program -> node.statements.forEach { compile(it, stack) }
        is AST.Conditional -> TODO("Conditions are not supported yet")
        is AST.Assignment -> {
            compile(node.toAssign, stack)
            stack += StackOp.St(node.variable.name)
        }
        is AST.WhileLoop -> TODO("While loops are not supported yet")
        is AST.ForLoop -> TODO("For loops are not supported yet")
        is AST.RepeatLoop -> TODO("Repeat loops are not supported yet")
    }
}

fun runStackMachine(operations: List<StackOp>) {
    val stack = ArrayList<Int>()
    val mem = HashMap<String, Int>()
    fun push(arg: Int) {
        stack += arg
    }

    fun pop(): Int {
        val res = stack.last()
        stack.remove(stack.size - 1)
        return res
    }
    operations.forEach {
        when (it) {
            is StackOp.Read -> push(readLine()!!.toInt())
            is StackOp.Write -> println(pop())
            is StackOp.Nop -> {
            }
            is StackOp.Push -> push(it.arg)
            is StackOp.Ld -> {
                val value = mem[it.arg] ?: throw IllegalStateException("No such variable $it.arg")
                push(value)
            }
            is StackOp.St -> mem[it.arg] = pop()
            is StackOp.Binop -> push(apply(pop(), pop(), it.arg))

        }
    }
}