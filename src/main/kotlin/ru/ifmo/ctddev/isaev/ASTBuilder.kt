package ru.ifmo.ctddev.isaev

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor
import ru.ifmo.ctddev.isaev.parser.LangParser
import ru.ifmo.ctddev.isaev.parser.LangVisitor

/**
 * @author iisaev
 */
class ASTBuilder : AbstractParseTreeVisitor<Node>(), LangVisitor<Node> {
    override fun visitProgram(ctx: LangParser.ProgramContext?): Node.Program {
        return Node.Program(ctx?.children!!.map { visitStatement(it as LangParser.StatementContext?) })
    }

    override fun visitStatement(ctx: LangParser.StatementContext?): Node {
        assert(ctx!!.childCount == 1)
        val child = ctx.getChild(0)
        return when (child) {
            is LangParser.AssignmentContext -> visitAssignment(child)
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

    override fun visitFunctionCall(ctx: LangParser.FunctionCallContext?): Node.FunctionCall {
        return Node.FunctionCall(
                visitVariable(ctx!!.variable()).name,
                emptyList()
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
        return visitAtom(ctx?.getChild(0) as LangParser.AtomContext?)
    }

    override fun visitAtom(ctx: LangParser.AtomContext?): Node {
        return visitFunctionCall(ctx?.getChild(0) as LangParser.FunctionCallContext?)
    }

    override fun visitVariable(ctx: LangParser.VariableContext?): Node.Variable {
        return Node.Variable(ctx!!.Var().text)
    }
}