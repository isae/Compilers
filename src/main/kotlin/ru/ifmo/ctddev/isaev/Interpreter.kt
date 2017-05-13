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

    fun interpretStatements(statements: List<AST>, ctx: MutableMap<String, Int>, funCtx: MutableMap<String, AST.FunctionDef>): Int {
        return statements.map { interpret(it, ctx, funCtx) }.lastOrNull() ?: 0
    }

    fun interpret(node: AST, ctx: MutableMap<String, Int>, funCtx: MutableMap<String, AST.FunctionDef>): Int {
        return when (node) {
            is AST.Skip -> 0
            is AST.Const -> node.number
            is AST.Variable -> ctx[node.name] as Int
            is AST.UnaryMinus -> -interpret(node.arg, ctx, funCtx)
            is AST.Binary -> apply(
                    interpret(node.left, ctx, funCtx),
                    interpret(node.right, ctx, funCtx),
                    node.op
            )
            is AST.FunctionCall -> {
                val callArgs = node.args.map {
                    when (it) {
                        is AST.Const -> it.number
                        else -> interpret(it, ctx, funCtx)
                    }
                }
                return when (node.functionName) {
                    "read" -> builtInRead(reader)
                    "write" -> {
                        callArgs.forEach { builtInWrite(it, writer) }
                        return 0
                    }
                    else -> {
                        val function = funCtx[node.functionName] ?: throw IllegalStateException("Invalid function name: $node.functionName")
                        val localCtx = HashMap<String, Int>()
                        if (function.argNames.size != callArgs.size) {
                            throw IllegalStateException("Argument number mismatch for function call $node.functionName")
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
                val isTrue = interpret(node.expr, ctx, funCtx) > 0
                if (isTrue) return interpretStatements(node.ifTrue, ctx, funCtx)
                for (elif in node.elifs) {
                    if (interpret(elif.expr, ctx, funCtx) > 0) {
                        return interpretStatements(elif.code, ctx, funCtx)
                    }
                }
                return interpretStatements(node.ifFalse, ctx, funCtx)
            }
            is AST.Assignment -> {
                ctx[node.variable.name] = interpret(node.toAssign, ctx, funCtx)
                return 0
            }
            is AST.WhileLoop -> {
                var last = 0
                while (interpret(node.expr, ctx, funCtx) > 0) {
                    last = interpretStatements(node.loop, ctx, funCtx)
                }
                return last
            }
            is AST.ForLoop -> {
                var last = 0
                interpretStatements(node.init, ctx, funCtx)
                while (interpret(node.expr, ctx, funCtx) != 0) {
                    last = interpretStatements(node.loop, ctx, funCtx)
                    interpretStatements(node.increment, ctx, funCtx)
                }
                return last
            }
            is AST.RepeatLoop -> {
                var last = interpretStatements(node.loop, ctx, funCtx)
                while (interpret(node.expr, ctx, funCtx) == 0) {
                    last = interpretStatements(node.loop, ctx, funCtx)
                }
                return last
            }
        }
    }

    fun run(program: AST): Int {
        init()
        return interpret(program, HashMap(), HashMap())
    }
}