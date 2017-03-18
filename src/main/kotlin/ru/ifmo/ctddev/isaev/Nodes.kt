package ru.ifmo.ctddev.isaev

/**
 * @author iisaev
 */
sealed class Node {
    class Const(val number: Int) : Node()
    class Variable(val name: String) : Node()
    class Addition(val addends: List<Node>) : Node()
    class Multiplication(val multipliers: List<Node>) : Node()
    class FunctionCall(val functionName: String, val args: List<Node>) : Node()
    class Program(val statements: List<Node>) : Node()
    class Assignment(val variable: Variable?, val toAssign: Node) : Node()
}
