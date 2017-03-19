package ru.ifmo.ctddev.isaev

/**
 * @author iisaev
 */
sealed class Node {
    class Const(val number: Int) : Node()
    class Skip : Node()
    class Variable(val name: String) : Node()
    sealed class Binary(val left: Node, val right: Node) : Node() {
        class Add(l: Node, r: Node) : Binary(l, r)
        class Sub(l: Node, r: Node) : Binary(l, r)
        class Mul(l: Node, r: Node) : Binary(l, r)
        class And(l: Node, r: Node) : Binary(l, r)
        class Or(l: Node, r: Node) : Binary(l, r)
        class Div(l: Node, r: Node) : Binary(l, r)
        class Mod(l: Node, r: Node) : Binary(l, r)
        class Eq(l: Node, r: Node) : Binary(l, r)
        class Neq(l: Node, r: Node) : Binary(l, r)
        class Lesser(l: Node, r: Node) : Binary(l, r)
        class Greater(l: Node, r: Node) : Binary(l, r)
        class Leq(l: Node, r: Node) : Binary(l, r)
        class Geq(l: Node, r: Node) : Binary(l, r)
    }

    class Dand(val left: Node, val right: Node) : Node()
    class Dor(val left: Node, val right: Node) : Node()
    class FunctionCall(val functionName: String, val args: List<Node>) : Node()
    class FunctionDef(val functionName: String, val argNames: List<String>, val body: List<Node>) : Node()
    class Program(val functions: List<FunctionDef>, val statements: List<Node>) : Node()
    class Conditional(val expr: Node, val ifTrue: List<Node>, val elifs: List<Elif>, val ifFalse: List<Node>) : Node()
    class Assignment(val variable: Variable, val toAssign: Node) : Node()
    class WhileLoop(val expr: Node, val loop: List<Node>) : Node()
    class UnaryMinus(val arg: Node) : Node()
    class ForLoop(val init: List<Node>, val expr: Node, val increment: List<Node>, val code: List<Node>) : Node()
    class RepeatLoop(val expr: Node, val loop: List<Node>) : Node()
}

data class Elif(val expr: Node, val code: List<Node>)