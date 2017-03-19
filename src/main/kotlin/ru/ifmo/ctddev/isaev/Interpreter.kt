package ru.ifmo.ctddev.isaev

/**
 * @author iisaev
 */
sealed class Node {
    abstract fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int
    fun interpret(): Int {
        return interpret(HashMap(), HashMap())
    }

    fun interpretStatements(statements: List<Node>, ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
        return statements.map { it.interpret(ctx, funCtx) }.lastOrNull() ?: 0
    }

    class Const(val number: Int) : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            return number
        }
    }

    class Skip : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            return 0
        }
    }

    class Variable(val name: String) : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            return ctx[name] as Int
        }
    }

    abstract class Binary(val left: Node, val right: Node) : Node() {
        abstract fun eval(left: Int, right: Int): Int
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            return eval(
                    left.interpret(ctx, funCtx),
                    right.interpret(ctx, funCtx)
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

    class And(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return left.and(right)
        }
    }

    class Or(l: Node, r: Node) : Binary(l, r) {
        override fun eval(left: Int, right: Int): Int {
            return left.or(right)
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

    class Dand(val left: Node, val right: Node) : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            val left = left.interpret(ctx, funCtx)
            if (left != 0) {
                return left.and(right.interpret(ctx, funCtx))
            } else {
                return 0
            }
        }
    }

    class Dor(val left: Node, val right: Node) : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            val left = left.interpret(ctx, funCtx)
            if (left != 0) {
                return left
            } else {
                return left.or(right.interpret(ctx, funCtx))
            }
        }
    }

    class FunctionCall(val functionName: String, val args: List<Node>) : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            //TODO: think about variables scoping
            val callArgs = args.map {
                when (it) {
                    is Const -> it.number
                    else -> it.interpret(ctx, funCtx)
                }
            }
            return when (functionName) {
                "read" -> readLine()!!.toInt()
                "write" -> {
                    callArgs.forEach(::println)
                    return 0
                }
                else -> {
                    val function = funCtx[functionName] ?: throw IllegalStateException("Invalid function name: $functionName")
                    val localCtx = HashMap<String, Int>()
                    if (function.argNames.size != callArgs.size) {
                        throw IllegalStateException("Argument number mismatch for function call $functionName")
                    }
                    function.argNames.forEachIndexed { i, s -> localCtx[s] = callArgs[i] }
                    return function.interpret(localCtx, funCtx)
                }
            }
        }
    }

    class FunctionDef(val functionName: String, val argNames: List<String>, val body: Program) : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            return body.interpret(ctx, funCtx)
        }

    }

    class Program(val functions: List<FunctionDef>, val statements: List<Node>) : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            functions.forEach {
                if (funCtx.containsKey(it.functionName)) {
                    throw IllegalStateException("Duplicate function: ${it.functionName}")
                } else {
                    funCtx.put(it.functionName, it)
                }
            }
            return interpretStatements(statements, ctx, funCtx)
        }
    }

    class Conditional(val expr: Node, val ifTrue: List<Node>, val ifFalse: List<Node>) : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            val isTrue = expr.interpret(ctx, funCtx) > 0
            return if (isTrue) interpretStatements(ifTrue, ctx, funCtx) else interpretStatements(ifFalse, ctx, funCtx)
        }
    }

    class Assignment(val variable: Variable, val toAssign: Node) : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            ctx[variable.name] = toAssign.interpret(ctx, funCtx)
            return 0
        }
    }

    class WhileLoop(val expr: Node, val loop: List<Node>) : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            var last = 0
            while (expr.interpret(ctx, funCtx) > 0) {
                last = interpretStatements(loop, ctx, funCtx)
            }
            return last
        }
    }

    class UnaryMinus(val arg: Node) : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            return -arg.interpret(ctx, funCtx)
        }
    }

    class ForLoop(val init: List<Node>, val expr: Node, val increment: List<Node>, val code: List<Node>) : Node() {
        override fun interpret(ctx: MutableMap<String, Int>, funCtx: MutableMap<String, FunctionDef>): Int {
            var last = 0
            interpretStatements(init, ctx, funCtx)
            while (expr.interpret(ctx, funCtx) != 0) {
                last = interpretStatements(code, ctx, funCtx)
                interpretStatements(increment, ctx, funCtx)
            }
            return last
        }
    }
}
