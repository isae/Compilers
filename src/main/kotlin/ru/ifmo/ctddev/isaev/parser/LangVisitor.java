// Generated from /Users/iisaev/IdeaProjects/Compilers/src/main/antlr4/ru/ifmo/ctddev/isaev/parser/Lang.g4 by ANTLR 4.6
package ru.ifmo.ctddev.isaev.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link LangParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface LangVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link LangParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(LangParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#codeBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCodeBlock(LangParser.CodeBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(LangParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(LangParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#whileLoop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileLoop(LangParser.WhileLoopContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#forLoop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLoop(LangParser.ForLoopContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#repeatLoop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepeatLoop(LangParser.RepeatLoopContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#cond}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCond(LangParser.CondContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#argList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgList(LangParser.ArgListContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#functionBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionBody(LangParser.FunctionBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(LangParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#functionDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDef(LangParser.FunctionDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(LangParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#addition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddition(LangParser.AdditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#multiplication}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplication(LangParser.MultiplicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom(LangParser.AtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link LangParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(LangParser.VariableContext ctx);
}