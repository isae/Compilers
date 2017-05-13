package ru.ifmo.ctddev.isaev

/**
 * @author iisaev
 */

fun apply(l: Val, r: Val, op: AST.Binary): Int {
    if (op.intOnly) {
        return apply(takeInt(l), takeInt(r), op.op)
    } else {
        if (l is Val.Character && r is Val.Character) {
            return apply(l.value, r.value, op.op)
        } else if (l is Val.Str && r is Val.Str) {
            return apply(l.value.toString(), r.value.toString(), op.op)
        } else if (l is Val.Number && r is Val.Number) {
            return apply(l.value, r.value, op.op)
        } else {
            throw IllegalStateException("Apply of operation ${op.op} is not supported for ${l::class.simpleName} and ${r::class.simpleName}")
        }
    }
}

fun apply(l: Int, r: Int, op: String): Int {
    return when (op) {
        "+" -> l + r
        "-" -> l - r
        "*" -> l * r
        "/" -> l / r
        "%" -> l % r
        "&" -> if (l != 0 && r != 0) 1 else 0
        "|" -> if (l == 0 && r == 0) 0 else 1
        "!!" -> if ((l == 0) == (r == 0)) 0 else 1
        "==" -> if (l == r) 1 else 0
        "!=" -> if (l != r) 1 else 0
        ">" -> if (l > r) 1 else 0
        ">=" -> if (l >= r) 1 else 0
        "<" -> if (l < r) 1 else 0
        "<=" -> if (l <= r) 1 else 0
        else -> TODO("Invalid operation $op for $l and $r")
    }
}

fun apply(l: String, r: String, op: String): Int {
    return when (op) {
        "==" -> if (l == r) 1 else 0
        "!=" -> if (l != r) 1 else 0
        ">" -> if (l > r) 1 else 0
        ">=" -> if (l >= r) 1 else 0
        "<" -> if (l < r) 1 else 0
        "<=" -> if (l <= r) 1 else 0
        else -> TODO("Invalid operation $op for $l and $r")
    }
}

fun apply(l: Char, r: Char, op: String): Int {
    return when (op) {
        "==" -> if (l == r) 1 else 0
        "!=" -> if (l != r) 1 else 0
        ">" -> if (l > r) 1 else 0
        ">=" -> if (l >= r) 1 else 0
        "<" -> if (l < r) 1 else 0
        "<=" -> if (l <= r) 1 else 0
        else -> TODO("Invalid operation $op for $l and $r")
    }
}

sealed class Val {
    class Void : Val() {
        override fun copy(): Val {
            return this;
        }
    }

    class Number(val value: Int) : Val() {
        override fun copy(): Val {
            return Number(value)
        }
    }

    class Array(val content: List<Val>) : Val() {
        override fun copy(): Val {
            return Array(content.map { it.copy() })
        }
    }

    class Character(val value: Char) : Val() {
        override fun copy(): Val {
            return Character(value)
        }
    }

    class Str(val value: StringBuilder) : Val() {
        override fun copy(): Val {
            return Str(value.toString())
        }

        constructor(strValue: String) : this(StringBuilder(strValue))
    }

    abstract fun copy(): Val
}

val ZERO = Val.Number(0)
val ONE = Val.Number(1)

val AST_ZERO = AST.Const(ZERO)
val AST_ONE = AST.Const(ONE)

sealed class AST {
    class Const(val value: Val) : AST()
    class Skip : AST()
    class Variable(val name: String) : AST()
    sealed class Binary(val left: AST, val right: AST, val op: String, val intOnly: Boolean) : AST() {
        class Add(l: AST, r: AST) : Binary(l, r, "+", true)
        class Sub(l: AST, r: AST) : Binary(l, r, "-", true)
        class Mul(l: AST, r: AST) : Binary(l, r, "*", true)
        class And(l: AST, r: AST) : Binary(l, r, "&", true)
        class Or(l: AST, r: AST) : Binary(l, r, "|", true)
        class Xor(l: AST, r: AST) : Binary(l, r, "|", true)
        class Div(l: AST, r: AST) : Binary(l, r, "/", true)
        class Mod(l: AST, r: AST) : Binary(l, r, "%", true)
        class Eq(l: AST, r: AST) : Binary(l, r, "==", false)
        class Neq(l: AST, r: AST) : Binary(l, r, "!=", false)
        class Lesser(l: AST, r: AST) : Binary(l, r, "<", false)
        class Greater(l: AST, r: AST) : Binary(l, r, ">", false)
        class Leq(l: AST, r: AST) : Binary(l, r, "<=", false)
        class Geq(l: AST, r: AST) : Binary(l, r, ">=", false)
    }

    class FunctionCall(val functionName: String, val args: List<AST>) : AST()
    class FunctionDef(val functionName: String, val argNames: List<String>, val body: List<AST>) : AST()
    class Program(val functions: List<FunctionDef>, val statements: List<AST>) : AST()
    class Conditional(val expr: AST, val ifTrue: List<AST>, val elifs: List<Elif>, val ifFalse: List<AST>) : AST()
    class Assignment(val variable: Variable, val toAssign: AST) : AST()
    class WhileLoop(val expr: AST, val loop: List<AST>) : AST()
    class UnaryMinus(val arg: AST) : AST()
    class ForLoop(val init: List<AST>, val expr: AST, val increment: List<AST>, val loop: List<AST>) : AST()
    class Array(val content: List<AST>) : AST()
    class RepeatLoop(val expr: AST, val loop: List<AST>) : AST()
}

data class Elif(val expr: AST, val code: List<AST>)
