package ru.ifmo.ctddev.isaev

/**
 * @author iisaev
 */

fun apply(l: Int, r: Int, op: String): Int {
    return when (op) {
        "+" -> l + r
        "-" -> l - r
        "*" -> l * r
        "/" -> l / r
        "%" -> l % r
        "&" -> l.and(r)
        "|" -> l.or(r)
        "==" -> if (l == r) 1 else 0
        "!=" -> if (l != r) 1 else 0
        ">" -> if (l > r) 1 else 0
        ">=" -> if (l >= r) 1 else 0
        "<" -> if (l < r) 1 else 0
        "<=" -> if (l <= r) 1 else 0
        else -> TODO("Unknown operation $op")
    }
}

sealed class AST {
    class Const(val number: Int) : AST()
    class Skip : AST()
    class Variable(val name: String) : AST()
    sealed class Binary(val left: AST, val right: AST, val op: String) : AST() {
        class Add(l: AST, r: AST) : Binary(l, r, "+")
        class Sub(l: AST, r: AST) : Binary(l, r, "-")
        class Mul(l: AST, r: AST) : Binary(l, r, "*")
        class And(l: AST, r: AST) : Binary(l, r, "&")
        class Or(l: AST, r: AST) : Binary(l, r, "|")
        class Div(l: AST, r: AST) : Binary(l, r, "/")
        class Mod(l: AST, r: AST) : Binary(l, r, "%")
        class Eq(l: AST, r: AST) : Binary(l, r, "==")
        class Neq(l: AST, r: AST) : Binary(l, r, "!=")
        class Lesser(l: AST, r: AST) : Binary(l, r, "<")
        class Greater(l: AST, r: AST) : Binary(l, r, ">")
        class Leq(l: AST, r: AST) : Binary(l, r, "<=")
        class Geq(l: AST, r: AST) : Binary(l, r, ">=")
    }

    class Dand(val left: AST, val right: AST) : AST()
    class Dor(val left: AST, val right: AST) : AST()
    class FunctionCall(val functionName: String, val args: List<AST>) : AST()
    class FunctionDef(val functionName: String, val argNames: List<String>, val body: List<AST>) : AST()
    class Program(val functions: List<FunctionDef>, val statements: List<AST>) : AST()
    class Conditional(val expr: AST, val ifTrue: List<AST>, val elifs: List<Elif>, val ifFalse: List<AST>) : AST()
    class Assignment(val variable: Variable, val toAssign: AST) : AST()
    class WhileLoop(val expr: AST, val loop: List<AST>) : AST()
    class UnaryMinus(val arg: AST) : AST()
    class ForLoop(val init: List<AST>, val expr: AST, val increment: List<AST>, val code: List<AST>) : AST()
    class RepeatLoop(val expr: AST, val loop: List<AST>) : AST()
}

data class Elif(val expr: AST, val code: List<AST>)
