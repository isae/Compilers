package ru.ifmo.ctddev.isaev

/**
 * @author iisaev
 */

fun interpret(program: Node): Int {
    return interpret(program, HashMap(), HashMap())
}

fun interpretStatements(statements: List<Node>, ctx: MutableMap<String, Int>, funCtx: MutableMap<String, Node.FunctionDef>): Int {
    return statements.map { interpret(it, ctx, funCtx) }.lastOrNull() ?: 0
}

fun interpret(node: Node, ctx: MutableMap<String, Int>, funCtx: MutableMap<String, Node.FunctionDef>): Int {
    return when (node) {
        is Node.Skip -> 0
        is Node.Const -> node.number
        is Node.Variable -> ctx[node.name] as Int
        is Node.UnaryMinus -> -interpret(node.arg, ctx, funCtx)
        is Node.Binary -> {
            val left = interpret(node.left, ctx, funCtx)
            val right = interpret(node.right, ctx, funCtx)
            return when (node) {
                is Node.Binary.Add -> left + right
                is Node.Binary.Sub -> left - right
                is Node.Binary.Mul -> left * right
                is Node.Binary.And -> left.and(right)
                is Node.Binary.Or -> left.or(right)
                is Node.Binary.Div -> left / right
                is Node.Binary.Mod -> left % right
                is Node.Binary.Eq -> if (left == right) 1 else 0
                is Node.Binary.Neq -> if (left != right) 1 else 0
                is Node.Binary.Lesser -> if (left < right) 1 else 0
                is Node.Binary.Greater -> if (left > right) 1 else 0
                is Node.Binary.Leq -> if (left <= right) 1 else 0
                is Node.Binary.Geq -> if (left >= right) 1 else 0
            }
        }
        is Node.Dand -> {
            val left = interpret(node.left, ctx, funCtx)
            if (left != 0) {
                return left.and(interpret(node.right, ctx, funCtx))
            } else {
                return 0
            }
        }
        is Node.Dor -> {
            val left = interpret(node.left, ctx, funCtx)
            if (left != 0) {
                return left
            } else {
                return left.or(interpret(node.right, ctx, funCtx))
            }
        }
        is Node.FunctionCall -> {
            val callArgs = node.args.map {
                when (it) {
                    is Node.Const -> it.number
                    else -> interpret(it, ctx, funCtx)
                }
            }
            return when (node.functionName) {
                "read" -> readLine()!!.toInt()
                "write" -> {
                    callArgs.forEach(::println)
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
        is Node.FunctionDef -> interpretStatements(node.body, ctx, funCtx)
        is Node.Program -> {
            node.functions.forEach {
                if (funCtx.containsKey(it.functionName)) {
                    throw IllegalStateException("Duplicate function: ${it.functionName}")
                } else {
                    funCtx.put(it.functionName, it)
                }
            }
            return interpretStatements(node.statements, ctx, funCtx)
        }
        is Node.Conditional -> {
            val isTrue = interpret(node.expr, ctx, funCtx) > 0
            if (isTrue) return interpretStatements(node.ifTrue, ctx, funCtx)
            for (elif in node.elifs) {
                if (interpret(elif.expr, ctx, funCtx) > 0) {
                    return interpretStatements(elif.code, ctx, funCtx)
                }
            }
            return interpretStatements(node.ifFalse, ctx, funCtx)
        }
        is Node.Assignment -> {
            ctx[node.variable.name] = interpret(node.toAssign, ctx, funCtx)
            return 0
        }
        is Node.WhileLoop -> {
            var last = 0
            while (interpret(node.expr, ctx, funCtx) > 0) {
                last = interpretStatements(node.loop, ctx, funCtx)
            }
            return last
        }
        is Node.ForLoop -> {
            var last = 0
            interpretStatements(node.init, ctx, funCtx)
            while (interpret(node.expr, ctx, funCtx) != 0) {
                last = interpretStatements(node.code, ctx, funCtx)
                interpretStatements(node.increment, ctx, funCtx)
            }
            return last
        }
        is Node.RepeatLoop -> {
            var last = interpretStatements(node.loop, ctx, funCtx)
            while (interpret(node.expr, ctx, funCtx) == 0) {
                last = interpretStatements(node.loop, ctx, funCtx)
            }
            return last
        }
    }
}