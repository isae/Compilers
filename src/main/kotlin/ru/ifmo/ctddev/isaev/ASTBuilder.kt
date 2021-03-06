package ru.ifmo.ctddev.isaev

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor
import org.antlr.v4.runtime.tree.TerminalNode
import ru.ifmo.ctddev.isaev.data.*
import ru.ifmo.ctddev.isaev.parser.LangParser
import ru.ifmo.ctddev.isaev.parser.LangVisitor
import java.util.*
import kotlin.collections.HashMap

/**
 * @author iisaev
 */
class ASTBuilder : AbstractParseTreeVisitor<AST>(), LangVisitor<AST> {
    override fun visitUnboxedArrayDeclaration(ctx: LangParser.UnboxedArrayDeclarationContext?): AST {
        val content = ArrayList<AST>()
        if (ctx!!.childCount == 2) {
            return AST.Array(content)
        }
        var i = 1
        while (i < ctx.childCount) {
            content += visitExpr(ctx.getChild(i) as LangParser.ExprContext)
            i += 2
        }
        return AST.Array(content)
    }

    override fun visitBoxedArrayDeclaration(ctx: LangParser.BoxedArrayDeclarationContext?): AST {
        val content = ArrayList<AST>()
        if (ctx!!.childCount == 2) {
            return AST.Array(content)
        }
        var i = 1
        while (i < ctx.childCount) {
            content += visitExpr(ctx.getChild(i) as LangParser.ExprContext)
            i += 2
        }
        return AST.Array(content)
    }

    override fun visitBoolConst(ctx: LangParser.BoolConstContext?): AST {
        if (ctx?.TRUE() != null) {
            return AST_ONE
        } else {
            return AST_ZERO
        }
    }

    override fun visitElifs(ctx: LangParser.ElifsContext?): AST {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun parseElifs(ctx: LangParser.ElifsContext?): List<Elif> {
        assert(ctx!!.childCount % 4 == 0)
        val result = ArrayList<Elif>()
        var pos = 0
        while ((pos * 4 + 3) < ctx.childCount) {
            val expr = visitExpr(ctx.getChild(pos * 4 + 1) as LangParser.ExprContext)
            val block = visitStatements((ctx.getChild(pos * 4 + 3) as LangParser.CodeBlockContext))
            result += Elif(expr, block)
            pos += 1
        }
        return result
    }

    override fun visitCodeBlock(ctx: LangParser.CodeBlockContext?): AST {
        TODO("not implemented")
    }

    fun visitStatements(ctx: LangParser.CodeBlockContext?): List<AST> {
        val statements = ArrayList<AST>()
        loop@ for (it in ctx!!.children) {
            if (it is TerminalNode) continue@loop
            val pair = parseStatement(it as LangParser.StatementContext?)
            statements += pair.first
            if (pair.second) {
                break@loop
            }
        }
        return statements
    }

    override fun visitProgram(ctx: LangParser.ProgramContext?): AST.Program {
        assert(ctx!!.childCount != 0) //empty program is not valid!
        val functionDefs = ArrayList<AST.FunctionDef>()
        var pos = 0
        while (ctx.getChild(pos) is LangParser.FunctionDefContext) {
            val child = ctx.getChild(pos) as LangParser.FunctionDefContext
            functionDefs += visitFunctionDef(child)
            ++pos
        }
        val mainBody = visitStatements(ctx.getChild(pos) as LangParser.CodeBlockContext)
        val main = AST.FunctionDef(MAIN_NAME, emptyList(), mainBody)
        functionDefs.add(main)
        val funCtx = HashMap<String, AST.FunctionDef>()
        functionDefs.forEach {
            if (funCtx.containsKey(it.functionName)) {
                throw IllegalStateException("Duplicate function: ${it.functionName}")
            } else {
                funCtx.put(it.functionName, it)
            }
        }
        return AST.Program(funCtx)
    }

    override fun visitStatement(ctx: LangParser.StatementContext?): AST {
        TODO("not implemented")
    }

    fun parseStatement(ctx: LangParser.StatementContext?): Pair<AST, Boolean> {
        assert(ctx!!.childCount == 1 || ctx.childCount == 2)
        val isLast = ctx.getChild(0) is TerminalNode && ctx.getChild(0).text == "return"
        val child = if (isLast) ctx.getChild(1) else ctx.getChild(0)
        return when (child) {
            is LangParser.AssignmentContext -> Pair(visitAssignment(child), isLast)
            is LangParser.FunctionCallContext -> Pair(visitFunctionCall(child), isLast)
            is LangParser.CondContext -> Pair(visitCond(child), isLast)
            is LangParser.WhileLoopContext -> Pair(visitWhileLoop(child), isLast)
            is LangParser.ForLoopContext -> Pair(visitForLoop(child), isLast)
            is LangParser.RepeatLoopContext -> Pair(visitRepeatLoop(child), isLast)
            is LangParser.ExprContext -> Pair(visitExpr(child), isLast)
            is TerminalNode -> {
                if (child.text == "skip") Pair(AST.Skip(), isLast) else
                    throw IllegalArgumentException("Invalid terminal statement: ${child.text}")
            }
            else -> throw IllegalArgumentException("Unknown node: ${child::class}")
        }
    }

    override fun visitAssignment(ctx: LangParser.AssignmentContext?): AST.Assignment {
        assert(ctx!!.childCount == 3)
        return AST.Assignment(
                visitVariable(ctx.variable()),
                visitExpr(ctx.expr())
        )
    }

    override fun visitWhileLoop(ctx: LangParser.WhileLoopContext?): AST {
        assert(ctx!!.childCount == 5)
        val expr = visitExpr(ctx.getChild(1) as LangParser.ExprContext?)
        val loop = visitStatements(ctx.getChild(3) as LangParser.CodeBlockContext)
        return AST.WhileLoop(expr, loop)
    }

    override fun visitForLoop(ctx: LangParser.ForLoopContext?): AST.ForLoop {
        assert(ctx!!.childCount == 9)
        val init = visitStatements(ctx.getChild(1) as LangParser.CodeBlockContext?)
        val expr = visitExpr(ctx.getChild(3) as LangParser.ExprContext?)
        val increment = visitStatements(ctx.getChild(5) as LangParser.CodeBlockContext?)
        val code = visitStatements(ctx.getChild(7) as LangParser.CodeBlockContext?)
        return AST.ForLoop(init, expr, increment, code)
    }

    override fun visitRepeatLoop(ctx: LangParser.RepeatLoopContext?): AST.RepeatLoop {
        assert(ctx!!.childCount == 4)
        val expr = visitExpr(ctx.getChild(3) as LangParser.ExprContext?)
        val code = visitStatements(ctx.getChild(1) as LangParser.CodeBlockContext?)
        return AST.RepeatLoop(expr, code)
    }

    override fun visitCond(ctx: LangParser.CondContext?): AST.Conditional {
        assert(ctx!!.childCount == 6 || ctx.childCount == 8)
        val expr = visitExpr(ctx.getChild(1) as LangParser.ExprContext?)
        val ifTrue = visitStatements(ctx.getChild(3) as LangParser.CodeBlockContext)
        val elifs = parseElifs(ctx.getChild(4) as LangParser.ElifsContext)
        val ifFalse = if (ctx.childCount == 8)
            visitStatements(ctx.getChild(6) as LangParser.CodeBlockContext) else
            emptyList<AST>()
        return AST.Conditional(expr, ifTrue, elifs, ifFalse)
    }

    override fun visitArgList(ctx: LangParser.ArgListContext?): AST {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun visitArgs(ctx: LangParser.ArgListContext?): List<AST> {
        return ctx!!.children
                ?.filter { it !is TerminalNode }
                ?.map { visitExpr(it as LangParser.ExprContext?) }
                ?: emptyList()
    }

    override fun visitFunctionCall(ctx: LangParser.FunctionCallContext?): AST.FunctionCall {
        val functionName = ctx!!.getChild(0).text ?: throw IllegalStateException("Null function name")
        val builtInTag = when (functionName) {
            "read" -> BuiltInTag.READ
            "write" -> BuiltInTag.WRITE
            "strlen" -> BuiltInTag.STRLEN
            "strget" -> BuiltInTag.STRGET
            "strset" -> BuiltInTag.STRSET
            "strsub" -> BuiltInTag.STRSUB
            "strdup" -> BuiltInTag.STRDUP
            "strcat" -> BuiltInTag.STRCAT
            "strcmp" -> BuiltInTag.STRCMP
            "strmake" -> BuiltInTag.STRMAKE
            "arrmake" -> BuiltInTag.ARRMAKE
            "Arrmake" -> BuiltInTag.ARRMAKE
            "arrlen" -> BuiltInTag.ARRLEN
            else -> null
        }
        val args = visitArgs(ctx.argList())
        return if (builtInTag == null) {
            AST.FunctionCall.UserDefined(functionName, args)
        } else {
            AST.FunctionCall.BuiltIn(builtInTag, args)
        }
    }

    override fun visitFunctionDef(ctx: LangParser.FunctionDefContext?): AST.FunctionDef {
        assert(ctx!!.childCount == 8)
        val functionName = ctx.getChild(1).text
        val args = visitArgs(ctx.getChild(3) as LangParser.ArgListContext)
        val body = visitStatements(ctx.getChild(6) as LangParser.CodeBlockContext)
        return AST.FunctionDef(
                functionName,
                args.map { it as AST.Variable }.map { it.name },
                body
        )
    }

    override fun visitExpr(ctx: LangParser.ExprContext?): AST {
        if (ctx!!.childCount == 1) {
            return visitAtom(ctx.getChild(0) as LangParser.AtomContext?)
        } else {
            val firstSign = ctx.getChild(0) is TerminalNode
            val isMinus = firstSign && ctx.getChild(0).text == "-"
            if (isMinus) {
                return AST.UnaryMinus(visitExpr(ctx.getChild(1) as LangParser.ExprContext))
            }
            val left = visitExpr(ctx.getChild(0) as LangParser.ExprContext)
            val right = visitExpr(ctx.getChild(2) as LangParser.ExprContext)
            val term = ctx.getChild(1) as TerminalNode
            return when (term.text) {
                "+" -> AST.Binary.Add(left, right)
                "-" -> AST.Binary.Sub(left, right)
                "*" -> AST.Binary.Mul(left, right)
                "/" -> AST.Binary.Div(left, right)
                "%" -> AST.Binary.Mod(left, right)
                "==" -> AST.Binary.Eq(left, right)
                "!=" -> AST.Binary.Neq(left, right)
                "<" -> AST.Binary.Lesser(left, right)
                ">" -> AST.Binary.Greater(left, right)
                "<=" -> AST.Binary.Leq(left, right)
                ">=" -> AST.Binary.Geq(left, right)
                "&" -> AST.Binary.And(left, right)
                "|" -> AST.Binary.Or(left, right)
                "!!" -> compileDoubleOr(left, right)
                "||" -> compileDoubleOr(left, right)
                "&&" -> AST.Conditional(
                        AST.Binary.Eq(left, AST_ZERO),
                        listOf(AST_ZERO),
                        emptyList(),
                        listOf(
                                AST.Conditional(
                                        AST.Binary.Eq(right, AST_ZERO),
                                        listOf(AST_ZERO),
                                        emptyList(),
                                        listOf(AST_ONE)
                                )
                        )
                )
                else -> throw IllegalStateException("Unknown term in expression: ${term.text}")
            }
        }
    }

    private fun compileDoubleOr(left: AST, right: AST): AST.Conditional {
        // TODO: here we treat numbers as booleans (replacing non-zero value with 1)
        // TODO: too fat AST for such simple literal
        return AST.Conditional(
                AST.Binary.Neq(left, AST_ZERO),
                listOf(AST_ONE),
                emptyList(),
                listOf(
                        AST.Conditional(
                                AST.Binary.Eq(right, AST_ZERO),
                                listOf(AST_ZERO),
                                emptyList(),
                                listOf(AST_ONE)
                        )
                )
        )
    }

    override fun visitAtom(ctx: LangParser.AtomContext?): AST {
        val child = ctx!!.getChild(0)
        return when (child) {
            is LangParser.VariableContext -> visitVariable(child)
            is LangParser.FunctionCallContext -> visitFunctionCall(child)
            is LangParser.BoolConstContext -> visitBoolConst(child)
            is LangParser.UnboxedArrayDeclarationContext -> visitUnboxedArrayDeclaration(child)
            is LangParser.BoxedArrayDeclarationContext -> visitBoxedArrayDeclaration(child)
            is TerminalNode -> {
                val nodeText = child.text
                return if (nodeText == "(") {
                    visitExpr(ctx.getChild(1) as LangParser.ExprContext?)
                } else {
                    val value: Val
                    if (nodeText.startsWith("\'")) {
                        if (nodeText.length != 3) {
                            throw IllegalStateException("Invalid character: ${nodeText}")
                        }
                        value = Val.Character(nodeText[1])
                    } else if (nodeText.startsWith("\"")) {
                        value = Val.Str(nodeText.substring(1..nodeText.length - 2))
                    } else {
                        value = Val.Number(nodeText.toInt())
                    }
                    return AST.Const(value)
                }
            }
            else -> throw IllegalArgumentException("Unknown node: ${child::class}")
        }
    }

    override fun visitVariable(ctx: LangParser.VariableContext?): AST.Variable {
        val variableName = ctx!!.Var().text
        if (ctx.childCount == 1) {
            return AST.Variable.Simple(variableName)
        } else {
            val indexes = ArrayList<AST>()
            var i = 2
            while (i < ctx.childCount) {
                indexes += visitExpr(ctx.getChild(i) as LangParser.ExprContext)
                i += 3
            }
            return AST.Variable.Index(variableName, indexes)
        }
    }
}