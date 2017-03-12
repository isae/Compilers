package ru.ifmo.ctddev.isaev

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.TerminalNode
import ru.ifmo.ctddev.isaev.parser.LangListener
import ru.ifmo.ctddev.isaev.parser.LangParser

/**
 * @author iisaev
 */
class Interpreter : LangListener {
    override fun visitErrorNode(p0: ErrorNode?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitTerminal(p0: TerminalNode?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterEveryRule(p0: ParserRuleContext?) {
// do nothing 
    }

    override fun exitEveryRule(p0: ParserRuleContext?) {
// do nothing 
    }

    override fun enterArgList(ctx: LangParser.ArgListContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterWhileLoop(ctx: LangParser.WhileLoopContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitWhileLoop(ctx: LangParser.WhileLoopContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitRepeatLoop(ctx: LangParser.RepeatLoopContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterAssignment(ctx: LangParser.AssignmentContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterCond(ctx: LangParser.CondContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterFunctionCall(ctx: LangParser.FunctionCallContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterStatement(ctx: LangParser.StatementContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitStatement(ctx: LangParser.StatementContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitExpr(ctx: LangParser.ExprContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterMultiplication(ctx: LangParser.MultiplicationContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterAtom(ctx: LangParser.AtomContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterAddition(ctx: LangParser.AdditionContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitAddition(ctx: LangParser.AdditionContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitArgList(ctx: LangParser.ArgListContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterRepeatLoop(ctx: LangParser.RepeatLoopContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitAssignment(ctx: LangParser.AssignmentContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterProgram(ctx: LangParser.ProgramContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitProgram(ctx: LangParser.ProgramContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitCond(ctx: LangParser.CondContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitFunctionCall(ctx: LangParser.FunctionCallContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterFunctionDef(ctx: LangParser.FunctionDefContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitFunctionDef(ctx: LangParser.FunctionDefContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterExpr(ctx: LangParser.ExprContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitMultiplication(ctx: LangParser.MultiplicationContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitAtom(ctx: LangParser.AtomContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterForLoop(ctx: LangParser.ForLoopContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exitForLoop(ctx: LangParser.ForLoopContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}