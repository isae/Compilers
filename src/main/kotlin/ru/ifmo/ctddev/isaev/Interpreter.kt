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

    fun interpretStatements(statements: List<AST>, ctx: MutableMap<String, Val>, funCtx: MutableMap<String, AST.FunctionDef>): Val {
        return statements.map { interpret(it, ctx, funCtx) }.lastOrNull() ?: Val.Void()
    }

    fun interpret(node: AST, ctx: MutableMap<String, Val>, funCtx: MutableMap<String, AST.FunctionDef>): Val {
        return when (node) {
            is AST.Skip -> Val.Void()
            is AST.Const -> node.value
            is AST.Variable.Simple -> ctx[node.name] ?: throw IllegalStateException("No such variable: ${node.name}")
            is AST.Variable.Index -> {
                val indexes = node.indexes
                        .map { interpret(it, ctx, funCtx) }
                        .map { takeInt(it) }
                val rootArray = parseArrayVariable(node, ctx, indexes)
                return rootArray.content[indexes.last()]
            }
            is AST.Array -> Val.Array(node.content.map { interpret(it, ctx, funCtx) }.toMutableList())
            is AST.UnaryMinus -> Val.Number(-takeInt(interpret(node.arg, ctx, funCtx)))
            is AST.Binary -> Val.Number(
                    apply(
                            interpret(node.left, ctx, funCtx),
                            interpret(node.right, ctx, funCtx),
                            node
                    )
            )
            is AST.FunctionCall -> {
                val callArgs = node.args.map {
                    when (it) {
                        is AST.Const -> it.value
                        else -> interpret(it, ctx, funCtx)
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
                        val value = takeString(interpret(node.args[0], ctx, funCtx))
                        return Val.Number(value.length)
                    }
                    "strget" -> {
                        assertArgNumber(node.functionName, 2, node.args.size)
                        val str = takeString(interpret(node.args[0], ctx, funCtx))
                        val index = takeInt(interpret(node.args[1], ctx, funCtx))
                        return Val.Character(str[index])
                    }
                    "strset" -> {
                        assertArgNumber(node.functionName, 3, node.args.size)
                        val str = takeString(interpret(node.args[0], ctx, funCtx))
                        val index = takeInt(interpret(node.args[1], ctx, funCtx))
                        val char = takeChar(interpret(node.args[2], ctx, funCtx))
                        str[index] = char
                        return Val.Void()
                    }
                    "strsub" -> {
                        assertArgNumber(node.functionName, 3, node.args.size)
                        val str = takeString(interpret(node.args[0], ctx, funCtx))
                        val from = takeInt(interpret(node.args[1], ctx, funCtx))
                        val to = takeInt(interpret(node.args[2], ctx, funCtx))
                        return Val.Str(str.substring(from, to))
                    }
                    "strdup" -> {
                        assertArgNumber(node.functionName, 1, node.args.size)
                        return Val.Str(takeString(interpret(node.args[0], ctx, funCtx)).toString())
                    }
                    "strcat" -> {
                        assertArgNumber(node.functionName, 2, node.args.size)
                        val fst = takeString(interpret(node.args[0], ctx, funCtx)).toString()
                        val snd = takeString(interpret(node.args[1], ctx, funCtx)).toString()
                        return Val.Str(fst + snd)
                    }
                    "strcmp" -> {
                        assertArgNumber(node.functionName, 2, node.args.size)
                        val fst = takeString(interpret(node.args[0], ctx, funCtx)).toString()
                        val snd = takeString(interpret(node.args[1], ctx, funCtx)).toString()
                        return Val.Number(fst.compareTo(snd))
                    }
                    "strmake" -> {
                        assertArgNumber(node.functionName, 2, node.args.size)
                        val numberOfChars = takeInt(interpret(node.args[0], ctx, funCtx))
                        val char = takeChar(interpret(node.args[1], ctx, funCtx))
                        return Val.Str(char.toString().repeat(numberOfChars))
                    }
                    "arrmake" -> {
                        assertArgNumber(node.functionName, 2, node.args.size)
                        val size = takeInt(interpret(node.args[0], ctx, funCtx))
                        val value = interpret(node.args[1], ctx, funCtx)
                        val result = ArrayList<Val>()
                        repeat(size, {
                            result.add(value.copy())
                        })
                        return Val.Array(result)
                    }
                    "arrlen" -> {
                        assertArgNumber(node.functionName, 1, node.args.size)
                        val value = interpret(node.args[0], ctx, funCtx);
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
            is AST.FunctionDef -> interpretStatements(node.body, ctx, funCtx)
            is AST.Program -> {
                node.functions.forEach {
                    if (funCtx.containsKey(it.functionName)) {
                        throw IllegalStateException("Duplicate function: ${it.functionName}")
                    } else {
                        funCtx.put(it.functionName, it)
                    }
                }
                return interpretStatements(node.statements, ctx, funCtx)
            }
            is AST.Conditional -> {
                val isTrue = takeInt(interpret(node.expr, ctx, funCtx)) > 0
                if (isTrue) return interpretStatements(node.ifTrue, ctx, funCtx)
                for (elif in node.elifs) {
                    if (takeInt(interpret(elif.expr, ctx, funCtx)) > 0) {
                        return interpretStatements(elif.code, ctx, funCtx)
                    }
                }
                return interpretStatements(node.ifFalse, ctx, funCtx)
            }
            is AST.Assignment -> {
                val variable = node.variable
                when (variable) {
                    is AST.Variable.Simple -> {
                        val result = interpret(node.toAssign, ctx, funCtx)
                        ctx[variable.name] = result
                        return result
                    }
                    is AST.Variable.Index -> {
                        val indexes = variable.indexes
                                .map { interpret(it, ctx, funCtx) }
                                .map { takeInt(it) }
                        val rootArray = parseArrayVariable(variable, ctx, indexes)
                        val result = interpret(node.toAssign, ctx, funCtx)
                        rootArray.content[indexes.last()] = result
                        return result
                    }
                }
            }
            is AST.WhileLoop -> {
                var last = Val.Void() as Val
                while (takeInt(interpret(node.expr, ctx, funCtx)) > 0) {
                    last = interpretStatements(node.loop, ctx, funCtx)
                }
                return last
            }
            is AST.ForLoop -> {
                var last = Val.Void() as Val
                interpretStatements(node.init, ctx, funCtx)
                while (takeInt(interpret(node.expr, ctx, funCtx)) != 0) {
                    last = interpretStatements(node.loop, ctx, funCtx)
                    interpretStatements(node.increment, ctx, funCtx)
                }
                return last
            }
            is AST.RepeatLoop -> {
                var last = interpretStatements(node.loop, ctx, funCtx)
                while (takeInt(interpret(node.expr, ctx, funCtx)) == 0) {
                    last = interpretStatements(node.loop, ctx, funCtx)
                }
                return last
            }
        }
    }

    private fun parseArrayVariable(variable: AST.Variable, ctx: MutableMap<String, Val>, indexes: List<Int>): Val.Array {
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

    fun run(program: AST): Val {
        init()
        return interpret(program, HashMap(), HashMap())
    }
}