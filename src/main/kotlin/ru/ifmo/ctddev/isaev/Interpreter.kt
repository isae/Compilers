package ru.ifmo.ctddev.isaev

/**
 * @author iisaev
 */
sealed class Node {
    abstract fun interpret(ctx: MutableMap<String, Any>): Int
    fun interpret(): Int {
        return interpret(HashMap())
    }

    class Const(val number: Int) : Node() {
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            return number
        }
    }

    class Variable(val name: String) : Node() {
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            return ctx[name] as Int
        }
    }

    class Add(val left: Node, val right: Node) : Node() {
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            return left.interpret(ctx) + right.interpret(ctx)
        }
    }

    class Sub(val left: Node, val right: Node) : Node() {
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            return left.interpret(ctx) - right.interpret(ctx)
        }
    }

    class Mul(val left: Node, val right: Node) : Node() {
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            return left.interpret(ctx) * right.interpret(ctx)
        }
    }

    class Div(val left: Node, val right: Node) : Node() {
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            return left.interpret(ctx) / right.interpret(ctx)
        }
    }

    class Mod(val left: Node, val right: Node) : Node() {
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            return left.interpret(ctx) % right.interpret(ctx)
        }
    }

    class FunctionCall(val functionName: String, val args: List<Node>) : Node() {
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            //TODO: think about variables scoping
            return when (functionName) {
                "read" -> readLine()!!.toInt()
                "write" -> {
                    args.map {
                        when (it) {
                            is Const -> it.number
                            else -> it.interpret(ctx)
                        }
                    }.forEach(::println)
                    return 0
                }
                else -> {
                    TODO("Custom function calls are not supported yet")
                }
            }
        }
    }

    class Program(val statements: List<Node>) : Node() {
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            statements.forEach { it.interpret(ctx) }
            return 0
        }
    }

    class Assignment(val variable: Variable, val toAssign: Node) : Node() {
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            ctx[variable.name] = toAssign.interpret(ctx)
            return 0
        }
    }
}
