package ru.ifmo.ctddev.isaev

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor
import org.antlr.v4.runtime.tree.TerminalNode
import ru.ifmo.ctddev.isaev.parser.LangParser
import ru.ifmo.ctddev.isaev.parser.LangVisitor

/**
 * @author iisaev
 */
class ASTBuilder : AbstractParseTreeVisitor<Node>(), LangVisitor<Node> {
    override fun visitFunctionBody(ctx: LangParser.FunctionBodyContext?): Node.Program {
        assert(ctx!!.childCount != 0) //empty function is not valid!
        val statements = ArrayList<Node>()
        var pos = 0
        var child = ctx.getChild(pos)
        while (!(child is TerminalNode && child.text.startsWith("return"))) {
            // searching for return
            if (child !is TerminalNode) {
                val stmt = ctx.getChild(pos) as LangParser.StatementContext
                statements.add(visitStatement(stmt))
            }
            ++pos
            child = ctx.getChild(pos)
        }
        statements.add(visitStatement(ctx.getChild(pos + 1) as LangParser.StatementContext))
        return Node.Program(emptyList(), statements)
    }

    override fun visitProgram(ctx: LangParser.ProgramContext?): Node.Program {
        assert(ctx!!.childCount != 0) //empty program is not valid!
        val functionDefs = ArrayList<Node.FunctionDef>()
        var pos = 0
        while (ctx.getChild(pos) is LangParser.FunctionDefContext) {
            val child = ctx.getChild(pos) as LangParser.FunctionDefContext
            functionDefs.add(visitFunctionDef(child))
            ++pos
        }
        return Node.Program(
                functionDefs,
                ctx.children!!
                        .drop(pos)
                        .filter { it !is TerminalNode }
                        .map { visitStatement(it as LangParser.StatementContext?) }
        )
    }

    override fun visitStatement(ctx: LangParser.StatementContext?): Node {
        assert(ctx!!.childCount == 1)
        val child = ctx.getChild(0)
        return when (child) {
            is LangParser.AssignmentContext -> visitAssignment(child)
            is LangParser.FunctionCallContext -> visitFunctionCall(child)
            is LangParser.CondContext -> visitCond(child)
            is LangParser.WhileLoopContext -> visitWhileLoop(child)
            is LangParser.ForLoopContext -> visitForLoop(child)
            is LangParser.RepeatLoopContext -> visitRepeatLoop(child)
            is LangParser.ExprContext -> visitExpr(child)
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
        val loop = visitProgram(ctx.getChild(3) as LangParser.ProgramContext)
        return Node.WhileLoop(expr, loop)
    }

    override fun visitForLoop(ctx: LangParser.ForLoopContext?): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitRepeatLoop(ctx: LangParser.RepeatLoopContext?): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitCond(ctx: LangParser.CondContext?): Node.Conditional {
        assert(ctx!!.childCount == 7)
        val expr = visitExpr(ctx.getChild(1) as LangParser.ExprContext?)
        val ifTrue = visitProgram(ctx.getChild(3) as LangParser.ProgramContext)
        val ifFalse = visitProgram(ctx.getChild(5) as LangParser.ProgramContext)
        return Node.Conditional(expr, ifTrue, ifFalse)
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
        val body = visitFunctionBody(ctx.getChild(6) as LangParser.FunctionBodyContext)
        return Node.FunctionDef(
                functionName.name,
                args.map { it as Node.Variable }.map { it.name },
                body
        )
    }

    override fun visitExpr(ctx: LangParser.ExprContext?): Node {
        if (ctx!!.childCount == 1) {
            return visitAddition(ctx.getChild(0) as LangParser.AdditionContext?)
        } else {
            var pos = 1
            var left = visitAddition(ctx.getChild(0) as LangParser.AdditionContext?)
            while (pos < ctx.childCount) {
                val term = ctx.getChild(pos) as TerminalNode
                val right = visitAddition(ctx.getChild(pos + 1) as LangParser.AdditionContext?)
                left = when (term.text) {
                    "==" -> Node.Eq(left, right)
                    "!=" -> Node.Neq(left, right)
                    "<" -> Node.Lesser(left, right)
                    "<=" -> Node.Leq(left, right)
                    ">" -> Node.Greater(left, right)
                    ">=" -> Node.Geq(left, right)
                    else -> throw IllegalStateException("Unknown term in expression: ${term.text}")
                }
                pos += 2
            }
            return left
        }
    }

    override fun visitAddition(ctx: LangParser.AdditionContext?): Node {
        if (ctx!!.childCount == 1) {
            return visitMultiplication(ctx.getChild(0) as LangParser.MultiplicationContext?)
        } else {
            var pos = 1
            var left = visitMultiplication(ctx.getChild(0) as LangParser.MultiplicationContext?)
            while (pos < ctx.childCount) {
                val term = ctx.getChild(pos) as TerminalNode
                val right = visitMultiplication(ctx.getChild(pos + 1) as LangParser.MultiplicationContext?)
                left = when (term.text) {
                    "+" -> Node.Add(left, right)
                    "-" -> Node.Sub(left, right)
                    else -> throw IllegalStateException("Unknown term in addition: ${term.text}")
                }
                pos += 2
            }
            return left
        }
    }

    override fun visitMultiplication(ctx: LangParser.MultiplicationContext?): Node {
        if (ctx!!.childCount == 1) {
            return visitAtom(ctx.getChild(0) as LangParser.AtomContext?)
        } else {
            var pos = 1
            var left = visitAtom(ctx.getChild(0) as LangParser.AtomContext?)
            while (pos < ctx.childCount) {
                val term = ctx.getChild(pos) as TerminalNode
                val right = visitAtom(ctx.getChild(pos + 1) as LangParser.AtomContext?)
                left = when (term.text) {
                    "*" -> Node.Mul(left, right)
                    "/" -> Node.Div(left, right)
                    "%" -> Node.Mod(left, right)
                    else -> throw IllegalStateException("Unknown term in multiplication: ${term.text}")
                }
                pos += 2
            }
            return left
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