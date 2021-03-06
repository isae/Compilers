package ru.ifmo.ctddev.isaev

import ru.ifmo.ctddev.isaev.data.AST
import ru.ifmo.ctddev.isaev.data.StackOp
import ru.ifmo.ctddev.isaev.data.Val
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.math.BigInteger
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


val random = Random()
val alreadyUsed = HashSet<String>()

fun getRandomLabel(): String {
    var result: String;
    do {
        result = "_${BigInteger(30, random).toString(32)}"
    } while (alreadyUsed.contains(result))
    alreadyUsed.add(result)
    return result
}

fun compile(node: AST): List<StackOp> {
    val result = ArrayList<StackOp>()
    compile(node, result)
    return result
}

private fun compile(nodes: List<AST>): List<StackOp> {
    val result = ArrayList<StackOp>()
    compile(nodes, result)
    return result
}

private fun compile(nodes: List<AST>, stack: MutableList<StackOp>) {
    nodes.forEach { compile(it, stack) }
}

private fun compile(node: AST, stack: MutableList<StackOp>) {
    when (node) {
        is AST.Skip -> {
        }
        is AST.Array -> {
            node.content.forEach {
                compile(it, stack)
            }
            stack += StackOp.MakeArr(node.content.size)
        }
        is AST.Const -> stack += StackOp.Push(node.value)
        is AST.Variable.Simple -> stack += StackOp.Ld(node.name)
        is AST.Variable.Index -> {
            compile(node.indexes, stack)
            stack += StackOp.LdArr(node.name, node.indexes.size)
        }
        is AST.UnaryMinus -> { // -a == 0-a
            stack += StackOp.Push(Val.Number(0))
            compile(node.arg, stack)
            stack += StackOp.Binop("-")
        }
        is AST.Binary -> {
            compile(node.left, stack)
            compile(node.right, stack)
            stack += StackOp.Binop(node.op)
        }
        is AST.FunctionCall -> {
            node.args.reversed().forEach { compile(it, stack) }
            when (node) {
                is AST.FunctionCall.BuiltIn -> stack += StackOp.BuiltIn(node.tag)
                is AST.FunctionCall.UserDefined -> {
                    stack += StackOp.Comm("Call ${node.name}")
                    stack += StackOp.Call(getLabelName(node.name), node.args.size)
                }
            }
        }
        is AST.FunctionDef -> {
            val labelName = getLabelName(node.functionName)

            stack += StackOp.Comm("Function '${node.functionName}' definition ...")
            stack += StackOp.Label(labelName)
            val localVariables = searchLocalVariables(node.body)
            localVariables.removeAll(node.argNames)
            stack += StackOp.Enter(node.argNames, localVariables)
            //node.argNames.reversed().forEach { stack += StackOp.St(it) }
            stack += StackOp.Comm("Function '${node.functionName}' body ...")
            compile(node.body, stack)
            stack += StackOp.Ret()
        }
        is AST.Program -> {
            node.functions.values.forEach { compile(it, stack) }
        }
        is AST.Conditional -> { //TODO: else is not mandatory
            val elifLabels = node.elifs.map { getRandomLabel() }
            stack += StackOp.Comm("If...")
            compile(node.expr, stack)
            val elseLabel = getRandomLabel()
            val afterIf = getRandomLabel()
            stack += if (elifLabels.isEmpty()) StackOp.Jif(elseLabel) else StackOp.Jif(elifLabels[0])
            stack += StackOp.Comm("Then...")
            compile(node.ifTrue, stack)
            stack += StackOp.Jump(afterIf)
            node.elifs.forEachIndexed { i, (expr, code) ->
                stack += StackOp.Label(elifLabels[i])
                stack += StackOp.Comm("Else If...")
                compile(expr, stack)
                stack += if (i == elifLabels.size - 1) StackOp.Jif(elseLabel) else StackOp.Jif(elifLabels[i + 1])
                stack += StackOp.Comm("Then...")
                compile(code, stack)
                stack += StackOp.Jump(afterIf)
            }
            stack += StackOp.Label(elseLabel)
            stack += StackOp.Comm("Else...")
            stack += compile(node.ifFalse)
            stack += StackOp.Label(afterIf)
            stack += StackOp.Comm("EndIf")
        }
        is AST.Assignment -> {
            compile(node.toAssign, stack)
            when (node.variable) {
                is AST.Variable.Simple -> stack += StackOp.St(node.variable.name)
                is AST.Variable.Index -> {
                    val variable = node.variable
                    compile(variable.indexes, stack)
                    stack += StackOp.StArr(variable.name, variable.indexes.size)
                }
            }
        }
        is AST.WhileLoop -> {
            val startLabel = getRandomLabel()
            val endLabel = getRandomLabel()
            stack += StackOp.Label(startLabel)
            stack += StackOp.Comm("While...")
            compile(node.expr, stack)
            stack += StackOp.Jif(endLabel)
            stack += StackOp.Comm("Do...")
            compile(node.loop, stack)
            stack += StackOp.Comm("EndWhile")
            stack += StackOp.Jump(startLabel)
            stack += StackOp.Label(endLabel)
        }
        is AST.ForLoop -> {
            compile(node.init, stack)
            val startLabel = getRandomLabel()
            val endLabel = getRandomLabel()
            stack += StackOp.Label(startLabel)
            stack += StackOp.Comm("For...")
            compile(node.expr, stack)
            stack += StackOp.Jif(endLabel)
            stack += StackOp.Comm("Do...")
            compile(node.loop, stack)
            stack += StackOp.Comm("Increment...")
            compile(node.increment, stack)
            stack += StackOp.Comm("EndFor")
            stack += StackOp.Jump(startLabel)
            stack += StackOp.Label(endLabel)
        }
        is AST.RepeatLoop -> {
            val startLabel = getRandomLabel()
            stack += StackOp.Label(startLabel)
            stack += StackOp.Comm("Repeat...")
            compile(node.loop, stack)
            stack += StackOp.Comm("Until...")
            compile(node.expr, stack) // we have zero if condition is unsuccessful
            stack += StackOp.Comm("EndRepeat")
            stack += StackOp.Jif(startLabel)
        }
    }
}

private fun getLabelName(functionName: String): String {
    if (functionName == MAIN_NAME) {
        return functionName
    } else {
        return "_function_$functionName"
    }
}

fun searchLocalVariables(body: List<AST>): MutableSet<String> {
    val results = HashSet<String>()
    body.forEach { searchLocalVariables(it, results) }
    return results
}

fun searchLocalVariables(node: AST, results: MutableSet<String>): Unit {
    when (node) {
        is AST.Skip -> {
        }
        is AST.Const -> {
        }
        is AST.Variable -> {
            results += node.name
        }
        is AST.UnaryMinus -> {
            searchLocalVariables(node.arg, results)
        }
        is AST.Binary -> {
            searchLocalVariables(node.left, results)
            searchLocalVariables(node.right, results)
        }
        is AST.FunctionCall -> {
            node.args.forEach { searchLocalVariables(it, results) }
        }
        is AST.FunctionDef -> {
        }
        is AST.Program -> {
        }
        is AST.Conditional -> {
            searchLocalVariables(node.expr, results)
            node.ifTrue.forEach { searchLocalVariables(it, results) }
            node.ifFalse.forEach { searchLocalVariables(it, results) }
            node.elifs.forEach { (expr, code) ->
                searchLocalVariables(expr, results)
                code.forEach { searchLocalVariables(it, results) }
            }
        }
        is AST.Assignment -> {
            results += node.variable.name
        }
        is AST.WhileLoop -> {
            searchLocalVariables(node.expr, results)
            node.loop.forEach { searchLocalVariables(it, results) }
        }
        is AST.ForLoop -> {
            searchLocalVariables(node.expr, results)
            node.init.forEach { searchLocalVariables(it, results) }
            node.increment.forEach { searchLocalVariables(it, results) }
            node.loop.forEach { searchLocalVariables(it, results) }
        }
        is AST.RepeatLoop -> {
            searchLocalVariables(node.expr, results)
            node.loop.forEach { searchLocalVariables(it, results) }
        }
    }
}

val MAIN_NAME = "main"

class StackMachine(val reader: BufferedReader = BufferedReader(InputStreamReader(System.`in`)),
                   val writer: PrintWriter = PrintWriter(OutputStreamWriter(System.out))) {

    private fun runStackMachine(operations: List<StackOp>) {
        val memory = HashMap<String, Val>()

        val stack = ArrayList<Val>()
        stack.push(Val.Number(operations.size)) // fake return address for main
        val functionStack = ArrayList<Pair<StackOp.Enter, Int>>()

        val labels = HashMap<String, Int>()
        operations.forEachIndexed { i, op ->
            if (op is StackOp.Label) {
                if (labels[op.label] != null) {
                    throw IllegalStateException("Duplicate label ${op.label}")
                }
                labels[op.label] = i
            }
        }
        var ip = labels[MAIN_NAME] ?: throw IllegalStateException("No such label: $MAIN_NAME")

        fun followIndexes(size: Int, name: String): Pair<Int, Val.Array> {
            val indexes = ArrayList<Int>()
            repeat(size, {
                indexes += takeInt(stack.pop())
            })
            var array = memory[name] as Val.Array
            indexes.dropLast(1).forEach {
                array = array.content[it] as Val.Array
            }
            return Pair(indexes.last(), array)
        }

        while (ip < operations.size) {
            val it = operations[ip]
            when (it) {
                is StackOp.BuiltIn -> {
                    val args = ArrayList<Val>()
                    repeat(it.tag.argSize, {
                        args += stack.pop()
                    })
                    val res = performBuiltIn(it.tag, reader, writer, args)
                    stack.push(res)
                }
                is StackOp.Nop -> {
                }
                is StackOp.Label -> {
                }
                is StackOp.Comm -> {
                    //println(it.comment)
                }
                is StackOp.Push -> stack.push(it.arg)
                is StackOp.Ld -> {
                    val value = memory[it.arg] ?: throw IllegalStateException("No such variable $it.arg")
                    stack.push(value)
                }
                is StackOp.St -> {
                    memory[it.arg] = stack.pop()
                }
                is StackOp.StArr -> {
                    val (index, array) = followIndexes(it.indexes, it.arg)
                    val value = stack.pop()
                    array.content[index] = value
                }
                is StackOp.LdArr -> {
                    val (index, array) = followIndexes(it.indexes, it.arg)
                    stack.push(array.content[index])
                }
                is StackOp.Binop -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    stack.push(Val.Number(apply(left, right, it.op)))
                }
                is StackOp.Jif -> {
                    val condition = takeInt(stack.pop())
                    if (condition == 0) {
                        ip = labels[it.label] ?: throw IllegalStateException("No such label ${it.label}")
                    }
                }
                is StackOp.MakeArr -> {
                    val array = ArrayList<Val>()
                    repeat(it.size, {
                        array += stack.pop()
                    })
                    stack.push(Val.Array(array.asReversed()))
                }
                is StackOp.Jump -> {
                    ip = labels[it.label] ?: throw IllegalStateException("No such label ${it.label}")
                }
                is StackOp.Call -> {
                    stack.push(Val.Number(ip)) //pushing return address, arguments are already on stack
                    val labelIp = labels[it.funName] ?: throw IllegalStateException("No such function '${it.funName}'")
                    ip = labelIp
                }
                is StackOp.Enter -> {
                    val returnAddress = stack.pop()
                    val varsToPreserve = TreeSet<String>()
                    varsToPreserve += it.argNames
                    varsToPreserve += it.localVariables
                    val localMem = HashMap<String, Val>()
                    it.argNames.forEach {
                        localMem[it] = stack.pop()
                    }
                    stack.push(returnAddress)
                    varsToPreserve.forEach {
                        stack.push(memory[it] ?: Val.Number(0))
                    }
                    memory += localMem
                    val base = stack.size
                    functionStack.push(Pair(it, base))
                }
                is StackOp.Ret -> {
                    val frame = functionStack.pop()
                    val enter = frame.first
                    val base = frame.second
                    val varsToRestore = TreeSet<String>()
                    varsToRestore += enter.argNames
                    varsToRestore += enter.localVariables

                    val returnValue = stack.pop()
                    stack.subList(base, stack.size).clear() //drop of elements after base

                    varsToRestore.reversed().forEach {
                        memory[it] = stack.pop()
                    }
                    val returnAddress = takeInt(stack.pop())
                    stack.push(returnValue)
                    ip = returnAddress
                }
            }
            ++ip
        }

    }

    fun run(operations: List<StackOp>) {
        init()
        runStackMachine(operations)
    }
}

private fun <E> MutableList<E>.push(value: E) {
    this += value
}

private fun <E> MutableList<E>.pop(): E {
    val res = last()
    removeAt(size - 1)
    return res
}
