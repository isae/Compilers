package ru.ifmo.ctddev.isaev

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter

/**
 * @author iisaev
 */

class Interpreter(val reader: BufferedReader = BufferedReader(InputStreamReader(System.`in`)),
                  val writer: PrintWriter = PrintWriter(OutputStreamWriter(System.out))) {

    fun interpret(node: AST, ctx: MutableMap<String, Val>, funCtx: MutableMap<String, AST.FunctionDef>): Val {
        fun performArrMake(node: AST.FunctionCall): Val.Array {
            val name = when (node) {
                is AST.FunctionCall.UserDefined -> node.name
                is AST.FunctionCall.BuiltIn -> node.tag.toString()
            }
            assertArgNumber(name, 2, node.args.size)
            val size = takeInt(interpret(node.args[0], ctx, funCtx))
            val value = interpret(node.args[1], ctx, funCtx)
            val result = ArrayList<Val>()
            repeat(size, {
                result.add(value.copy())
            })
            return Val.Array(result)
        }

        fun parseArrayVariable(variable: AST.Variable, indexes: List<Int>): Val.Array {
            var arrName = variable.name
            var rootArray = ctx[variable.name] as? Val.Array
                    ?: throw IllegalStateException("$arrName is not an array")
            indexes.take(indexes.size - 1).map({ idx ->
                arrName = "$arrName[$idx]"
                rootArray = rootArray.content[idx]as? Val.Array
                        ?: throw IllegalStateException("$arrName is not an array")
            })
            return rootArray
        }

        fun interpretStatements(statements: List<AST>): Val {
            return statements.map { interpret(it, ctx, funCtx) }.lastOrNull() ?: Val.Void()
        }

        fun interpret(node: AST): Val {
            return when (node) {
                is AST.Skip -> Val.Void()
                is AST.Const -> node.value
                is AST.Variable.Simple -> ctx[node.name] ?: throw IllegalStateException("No such variable: ${node.name}")
                is AST.Variable.Index -> {
                    val indexes = node.indexes
                            .map { interpret(it) }
                            .map { takeInt(it) }
                    val rootArray = parseArrayVariable(node, indexes)
                    return rootArray.content[indexes.last()]
                }
                is AST.Array -> Val.Array(node.content.map { interpret(it) }.toMutableList())
                is AST.UnaryMinus -> Val.Number(-takeInt(interpret(node.arg)))
                is AST.Binary -> Val.Number(
                        apply(
                                interpret(node.left),
                                interpret(node.right),
                                node.op
                        )
                )
                is AST.FunctionCall -> {
                    val callArgs = node.args.map {
                        when (it) {
                            is AST.Const -> it.value
                            else -> interpret(it)
                        }
                    }
                    return when (node) {
                        is AST.FunctionCall.BuiltIn -> performBuiltIn(node.tag, reader, writer, callArgs)
                        is AST.FunctionCall.UserDefined -> {
                            val function = funCtx[node.name] ?: throw IllegalStateException("Invalid function name: ${node.name}")
                            val localCtx = HashMap<String, Val>()
                            if (function.argNames.size != callArgs.size) {
                                throw IllegalStateException("Argument number mismatch for function call ${node.name}")
                            }
                            function.argNames.forEachIndexed { i, s -> localCtx[s] = callArgs[i] }
                            return interpret(function, localCtx, funCtx)
                        }
                    }
                }
                is AST.FunctionDef -> interpretStatements(node.body)
                is AST.Program -> {
                    node.functions.forEach {
                        if (funCtx.containsKey(it.functionName)) {
                            throw IllegalStateException("Duplicate function: ${it.functionName}")
                        } else {
                            funCtx.put(it.functionName, it)
                        }
                    }
                    return interpretStatements(node.statements)
                }
                is AST.Conditional -> {
                    val isTrue = takeInt(interpret(node.expr)) > 0
                    if (isTrue) return interpretStatements(node.ifTrue)
                    for (elif in node.elifs) {
                        if (takeInt(interpret(elif.expr)) > 0) {
                            return interpretStatements(elif.code)
                        }
                    }
                    return interpretStatements(node.ifFalse)
                }
                is AST.Assignment -> {
                    val variable = node.variable
                    when (variable) {
                        is AST.Variable.Simple -> {
                            val result = interpret(node.toAssign)
                            ctx[variable.name] = result
                            return result
                        }
                        is AST.Variable.Index -> {
                            val indexes = variable.indexes
                                    .map { interpret(it) }
                                    .map { takeInt(it) }
                            val rootArray = parseArrayVariable(variable, indexes)
                            val result = interpret(node.toAssign)
                            rootArray.content[indexes.last()] = result
                            return result
                        }
                    }
                }
                is AST.WhileLoop -> {
                    var last = Val.Void() as Val
                    while (takeInt(interpret(node.expr)) > 0) {
                        last = interpretStatements(node.loop)
                    }
                    return last
                }
                is AST.ForLoop -> {
                    var last = Val.Void() as Val
                    interpretStatements(node.init)
                    while (takeInt(interpret(node.expr)) != 0) {
                        last = interpretStatements(node.loop)
                        interpretStatements(node.increment)
                    }
                    return last
                }
                is AST.RepeatLoop -> {
                    var last = interpretStatements(node.loop)
                    while (takeInt(interpret(node.expr)) == 0) {
                        last = interpretStatements(node.loop)
                    }
                    return last
                }
            }
        }
        return interpret(node)
    }

    fun run(program: AST): Val {
        init()
        return interpret(program, HashMap(), HashMap())
    }
}