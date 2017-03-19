package ru.ifmo.ctddev.isaev

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor
import org.antlr.v4.runtime.tree.TerminalNode
import ru.ifmo.ctddev.isaev.parser.LangParser
import ru.ifmo.ctddev.isaev.parser.LangVisitor

/**
 * @author iisaev
 */
class ASTBuilder : AbstractParseTreeVisitor<Node>(), LangVisitor<Node> {
    override fun visitElifs(ctx: LangParser.ElifsContext?): Node {
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

    override fun visitCodeBlock(ctx: LangParser.CodeBlockContext?): Node {
        TODO("not implemented")
    }

    fun visitStatements(ctx: LangParser.CodeBlockContext?): List<Node> {
        val statements = ArrayList<Node>()
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

    override fun visitProgram(ctx: LangParser.ProgramContext?): Node.Program {
        assert(ctx!!.childCount != 0) //empty program is not valid!
        val functionDefs = ArrayList<Node.FunctionDef>()
        var pos = 0
        while (ctx.getChild(pos) is LangParser.FunctionDefContext) {
            val child = ctx.getChild(pos) as LangParser.FunctionDefContext
            functionDefs += visitFunctionDef(child)
            ++pos
        }
        return Node.Program(
                functionDefs,
                visitStatements(ctx.getChild(pos) as LangParser.CodeBlockContext)
        )
    }

    override fun visitStatement(ctx: LangParser.StatementContext?): Node {
        TODO("not implemented")
    }

    fun parseStatement(ctx: LangParser.StatementContext?): Pair<Node, Boolean> {
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
                if (child.text == "skip") Pair(Node.Skip(), isLast) else
                    throw IllegalArgumentException("Invalid terminal statement: ${child.text}")
            }
            else -> throw IllegalArgumentException("Unknown node: ${child::class}")
        }
    }

    override fun visitAssignment(ctx: LangParser.AssignmentContext?): Node.Assignment {
        assert(ctx!!.childCount == 3)
        return Node.Assignment(
                visitVariable(ctx.variable()),
                visitExpr(ctx.expr())
        )
    }

    override fun visitWhileLoop(ctx: LangParser.WhileLoopContext?): Node {
        assert(ctx!!.childCount == 5)
        val expr = visitExpr(ctx.getChild(1) as LangParser.ExprContext?)
        val loop = visitStatements(ctx.getChild(3) as LangParser.CodeBlockContext)
        return Node.WhileLoop(expr, loop)
    }

    override fun visitForLoop(ctx: LangParser.ForLoopContext?): Node.ForLoop {
        assert(ctx!!.childCount == 9)
        val init = visitStatements(ctx.getChild(1) as LangParser.CodeBlockContext?)
        val expr = visitExpr(ctx.getChild(3) as LangParser.ExprContext?)
        val increment = visitStatements(ctx.getChild(5) as LangParser.CodeBlockContext?)
        val code = visitStatements(ctx.getChild(7) as LangParser.CodeBlockContext?)
        return Node.ForLoop(init, expr, increment, code)
    }

    override fun visitRepeatLoop(ctx: LangParser.RepeatLoopContext?): Node.RepeatLoop {
        assert(ctx!!.childCount == 4)
        val expr = visitExpr(ctx.getChild(3) as LangParser.ExprContext?)
        val code = visitStatements(ctx.getChild(1) as LangParser.CodeBlockContext?)
        return Node.RepeatLoop(expr, code)
    }

    override fun visitCond(ctx: LangParser.CondContext?): Node.Conditional {
        assert(ctx!!.childCount == 6 || ctx.childCount == 8)
        val expr = visitExpr(ctx.getChild(1) as LangParser.ExprContext?)
        val ifTrue = visitStatements(ctx.getChild(3) as LangParser.CodeBlockContext)
        val elifs = parseElifs(ctx.getChild(4) as LangParser.ElifsContext)
        val ifFalse = if (ctx.childCount == 8)
            visitStatements(ctx.getChild(6) as LangParser.CodeBlockContext) else
            emptyList<Node>()
        return Node.Conditional(expr, ifTrue, elifs, ifFalse)
    }

    override fun visitArgList(ctx: LangParser.ArgListContext?): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun visitArgs(ctx: LangParser.ArgListContext?): List<Node> {
        return ctx!!.children
                ?.filter { it !is TerminalNode }
                ?.map { visitExpr(it as LangParser.ExprContext?) }
                ?: emptyList()
    }

    override fun visitFunctionCall(ctx: LangParser.FunctionCallContext?): Node.FunctionCall {
        return Node.FunctionCall(
                visitVariable(ctx!!.variable()).name,
                visitArgs(ctx.argList())
        )
    }

    override fun visitFunctionDef(ctx: LangParser.FunctionDefContext?): Node.FunctionDef {
        assert(ctx!!.childCount == 8)
        val functionName = visitVariable(ctx.getChild(1) as LangParser.VariableContext)
        val args = visitArgs(ctx.getChild(3) as LangParser.ArgListContext)
        val body = visitStatements(ctx.getChild(6) as LangParser.CodeBlockContext)
        return Node.FunctionDef(
                functionName.name,
                args.map { it as Node.Variable }.map { it.name },
                body
        )
    }

    override fun visitExpr(ctx: LangParser.ExprContext?): Node {
        if (ctx!!.childCount == 1) {
            return visitAtom(ctx.getChild(0) as LangParser.AtomContext?)
        } else {
            val firstSign = ctx.getChild(0) is TerminalNode
            val isMinus = firstSign && ctx.getChild(0).text == "-"
            if (isMinus) {
                return Node.UnaryMinus(visitExpr(ctx.getChild(1) as LangParser.ExprContext))
            }
            val left = visitExpr(ctx.getChild(0) as LangParser.ExprContext)
            val right = visitExpr(ctx.getChild(2) as LangParser.ExprContext)
            val term = ctx.getChild(1) as TerminalNode
            return when (term.text) {
                "+" -> Node.Binary.Add(left, right)
                "-" -> Node.Binary.Sub(left, right)
                "*" -> Node.Binary.Mul(left, right)
                "/" -> Node.Binary.Div(left, right)
                "%" -> Node.Binary.Mod(left, right)
                "==" -> Node.Binary.Eq(left, right)
                "!=" -> Node.Binary.Neq(left, right)
                "<" -> Node.Binary.Lesser(left, right)
                ">" -> Node.Binary.Greater(left, right)
                "<=" -> Node.Binary.Leq(left, right)
                ">=" -> Node.Binary.Geq(left, right)
                "&" -> Node.Binary.And(left, right)
                "|" -> Node.Binary.Or(left, right)
                "&&" -> Node.Dand(left, right)
                "||" -> Node.Dor(left, right)
                else -> throw IllegalStateException("Unknown term in expression: ${term.text}")
            }
        }
    }

    override fun visitAtom(ctx: LangParser.AtomContext?): Node {
        val child = ctx!!.getChild(0)
        return when (child) {
            is LangParser.VariableContext -> visitVariable(child)
            is LangParser.FunctionCallContext -> visitFunctionCall(child)
            is TerminalNode -> {
                val nodeText = child.text
                return when (nodeText) {
                    "(" -> visitExpr(ctx.getChild(1) as LangParser.ExprContext?)
                    else -> Node.Const(nodeText.toInt())
                }
            }
            else -> throw IllegalArgumentException("Unknown node: ${child::class}")
        }
    }

    override fun visitVariable(ctx: LangParser.VariableContext?): Node.Variable {
        return Node.Variable(ctx!!.Var().text)
    }
}