package ru.ifmo.ctddev.isaev

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor
import org.antlr.v4.runtime.tree.TerminalNode
import ru.ifmo.ctddev.isaev.parser.LangParser
import ru.ifmo.ctddev.isaev.parser.LangVisitor

/**
 * @author iisaev
 */
class ASTBuilder : AbstractParseTreeVisitor<Node>(), LangVisitor<Node> {
    override fun visitProgram(ctx: LangParser.ProgramContext?): Node.Program {
        return Node.Program(
                ctx?.children!!
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitForLoop(ctx: LangParser.ForLoopContext?): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitRepeatLoop(ctx: LangParser.RepeatLoopContext?): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitCond(ctx: LangParser.CondContext?): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    override fun visitFunctionDef(ctx: LangParser.FunctionDefContext?): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitExpr(ctx: LangParser.ExprContext?): Node {
        return visitAddition(ctx?.getChild(0) as LangParser.AdditionContext?)
    }

    override fun visitAddition(ctx: LangParser.AdditionContext?): Node {
        return visitMultiplication(ctx?.getChild(0) as LangParser.MultiplicationContext?)
    }

    override fun visitMultiplication(ctx: LangParser.MultiplicationContext?): Node {
        if (ctx!!.childCount == 1) {
            return visitAtom(ctx.getChild(0) as LangParser.AtomContext?)
        } else {
            return Node.Multiplication(ctx.children!!
                    .filter { it !is TerminalNode }
                    .map { visitAtom(it as LangParser.AtomContext?) })
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
                    "(" -> visitExpr(child.getChild(1) as LangParser.ExprContext?)
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