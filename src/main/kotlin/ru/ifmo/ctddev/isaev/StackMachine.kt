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

fun compile(node: Node, stack: MutableList<StackOp>) {
    when (node) {
        is Node.Skip -> stack += StackOp.Nop()
        is Node.Const -> stack += StackOp.Push(node.number)
        is Node.Variable -> stack += StackOp.Ld(node.name)
        is Node.UnaryMinus -> { // -a == 0-a
            stack += StackOp.Push(0)
            compile(node.arg)
            stack += StackOp.Binop("-")
        }
        is Node.Binary -> {
            compile(node.left)
            compile(node.right)
            stack += StackOp.Binop(node.op)
        }
        is Node.Dand -> {
            TODO("&& is not supported yet")
        }
        is Node.Dor -> {
            TODO("|| is not supported yet")
        }
        is Node.FunctionCall -> {
            when (node.functionName) {
                "read" -> stack += StackOp.Read()
                "write" -> {
                    if (node.args.size != 1) {
                        TODO("Vararg write is not supported yet")
                    }
                    compile(node.args[0])
                    stack += StackOp.Write()
                }
                else -> TODO("Function calls are not supported yet")
            }
        }
        is Node.FunctionDef -> TODO("Function definitions are not supported yet")
        is Node.Program -> node.statements.forEach { compile(it, stack) }
        is Node.Conditional -> TODO("Conditions are not supported yet")
        is Node.Assignment -> {
            compile(node.toAssign)
            stack += StackOp.St(node.variable.name)
        }
        is Node.WhileLoop -> TODO("While loops are not supported yet")
        is Node.ForLoop -> TODO("For loops are not supported yet")
        is Node.RepeatLoop -> TODO("Repeat loops are not supported yet")
    }
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