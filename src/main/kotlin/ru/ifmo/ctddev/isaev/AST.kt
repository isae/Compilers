package ru.ifmo.ctddev.isaev

/**
 * @author iisaev
 */
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

    class Array(val content: MutableList<Val>) : Val() {
        override fun copy(): Val {
            return Array(content.map { it.copy() }.toMutableList())
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
    sealed class Variable(val name: String) : AST() {
        class Simple(name: String) : Variable(name)
        class Index(name: String, val indexes: List<AST>) : Variable(name)
    }

    sealed class Binary(val left: AST, val right: AST, val op: String) : AST() {
        class Add(l: AST, r: AST) : Binary(l, r, "+")
        class Sub(l: AST, r: AST) : Binary(l, r, "-")
        class Mul(l: AST, r: AST) : Binary(l, r, "*")
        class And(l: AST, r: AST) : Binary(l, r, "&")
        class Or(l: AST, r: AST) : Binary(l, r, "|")
        class Div(l: AST, r: AST) : Binary(l, r, "/")
        class Mod(l: AST, r: AST) : Binary(l, r, "%")
        //
        class Eq(l: AST, r: AST) : Binary(l, r, "==")
        class Neq(l: AST, r: AST) : Binary(l, r, "!=")
        class Lesser(l: AST, r: AST) : Binary(l, r, "<")
        class Greater(l: AST, r: AST) : Binary(l, r, ">")
        class Leq(l: AST, r: AST) : Binary(l, r, "<=")
        class Geq(l: AST, r: AST) : Binary(l, r, ">=")
    }

    class FunctionCall(val functionName: String, val args: List<AST>) : AST()
    class BuiltIn(val args: List<AST>, val tag: BuiltInTag);
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
