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

    abstract class Binary(val left: Node, val right: Node) : Node() {
        abstract fun eval(left: Int, right: Int): Int
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            return eval(
                    left.interpret(ctx),
                    right.interpret(ctx)
            )
        }
    }

    class Add(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return left + right
        }
    }

    class Sub(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return left - right
        }
    }

    class Mul(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return left * right
        }
    }

    class Div(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return left / right
        }
    }

    class Mod(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return left % right
        }
    }

    class Eq(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return if (left == right) 1 else 0
        }
    }

    class Neq(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return if (left != right) 1 else 0
        }
    }

    class Lesser(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return if (left < right) 1 else 0
        }
    }

    class Greater(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return if (left > right) 1 else 0
        }
    }

    class Leq(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return if (left <= right) 1 else 0
        }
    }

    class Geq(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return if (left >= right) 1 else 0
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
            return statements.map { it.interpret(ctx) }.lastOrNull() ?: 0;
        }
    }

    class Conditional(val expr: Node, val ifTrue: Node, val ifFalse: Node) : Node() {
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            val isTrue = expr.interpret(ctx) > 0
            return if (isTrue) ifTrue.interpret(ctx) else ifFalse.interpret(ctx);
        }
    }

    class Assignment(val variable: Variable, val toAssign: Node) : Node() {
        override fun interpret(ctx: MutableMap<String, Any>): Int {
            ctx[variable.name] = toAssign.interpret(ctx)
            return 0
        }
    }
}
