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
            assertArgNumber(node.functionName, 2, node.args.size)
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
                                node
                        )
                )
                is AST.FunctionCall -> {
                    val callArgs = node.args.map {
                        when (it) {
                            is AST.Const -> it.value
                            else -> interpret(it)
                        }
                    }
                    return when (node.functionName) {
                        "read" -> builtInRead(reader)
                        "write" -> {
                            callArgs.forEach { builtInWrite(it, writer) }
                            return Val.Void()
                        }
                        "strlen" -> {
                            assertArgNumber(node.functionName, 1, node.args.size)
                            val value = takeString(interpret(node.args[0]))
                            return Val.Number(value.length)
                        }
                        "strget" -> {
                            assertArgNumber(node.functionName, 2, node.args.size)
                            val str = takeString(interpret(node.args[0]))
                            val index = takeInt(interpret(node.args[1]))
                            return Val.Character(str[index])
                        }
                        "strset" -> {
                            assertArgNumber(node.functionName, 3, node.args.size)
                            val str = takeString(interpret(node.args[0]))
                            val index = takeInt(interpret(node.args[1]))
                            val char = takeChar(interpret(node.args[2]))
                            str[index] = char
                            return Val.Void()
                        }
                        "strsub" -> {
                            assertArgNumber(node.functionName, 3, node.args.size)
                            val str = takeString(interpret(node.args[0]))
                            val from = takeInt(interpret(node.args[1]))
                            val length = takeInt(interpret(node.args[2]))
                            return Val.Str(str.substring(from, from + length))
                        }
                        "strdup" -> {
                            assertArgNumber(node.functionName, 1, node.args.size)
                            return Val.Str(takeString(interpret(node.args[0])).toString())
                        }
                        "strcat" -> {
                            assertArgNumber(node.functionName, 2, node.args.size)
                            val fst = takeString(interpret(node.args[0])).toString()
                            val snd = takeString(interpret(node.args[1])).toString()
                            return Val.Str(fst + snd)
                        }
                        "strcmp" -> {
                            assertArgNumber(node.functionName, 2, node.args.size)
                            val fst = takeString(interpret(node.args[0])).toString()
                            val snd = takeString(interpret(node.args[1])).toString()
                            return Val.Number(fst.compareTo(snd))
                        }
                        "strmake" -> {
                            assertArgNumber(node.functionName, 2, node.args.size)
                            val numberOfChars = takeInt(interpret(node.args[0]))
                            val char = takeChar(interpret(node.args[1]))
                            return Val.Str(char.toString().repeat(numberOfChars))
                        }
                        "Arrmake" -> return performArrMake(node)
                        "arrmake" -> return performArrMake(node)
                        "arrlen" -> {
                            assertArgNumber(node.functionName, 1, node.args.size)
                            val value = interpret(node.args[0]);
                            val array = value as? Val.Array ?: throw IllegalStateException("An argument of arrlen must be an array; found ${value::class.simpleName}")
                            return Val.Number(array.content.size)
                        }
                        else -> {
                            val function = funCtx[node.functionName] ?: throw IllegalStateException("Invalid function name: ${node.functionName}")
                            val localCtx = HashMap<String, Val>()
                            if (function.argNames.size != callArgs.size) {
                                throw IllegalStateException("Argument number mismatch for function call ${node.functionName}")
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