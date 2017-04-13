package ru.ifmo.ctddev.isaev

import java.util.*

val macros = """
%macro clib_prolog 1
mov ebx, esp 
and esp, 0xFFFFFFF0
sub esp, 12    
push ebx         
sub esp, %1
%endmacro

%macro clib_epilog 1
add esp, %1
pop ebx        
mov esp, ebx
%endmacro
    """

val externs = """
extern _printf
extern _scanf
extern _gets
"""
val prefix = """
SECTION .text
GLOBAL _main
    """

val rodata = """
SECTION .rodata
format_in: db "%d", 0
format_out: db "%d", 10, 0
"""

val suffix = """
    mov eax, 0
    ret
"""

sealed class AsmOp {
    class Nop : AsmOp() {
        override fun toString(): String {
            return "NOP"
        }
    }

    class Push(val arg: Int) : AsmOp() {
        override fun toString(): String {
            return "PUSH $arg"
        }
    }

    class Ld(val arg: String) : AsmOp() {
        override fun toString(): String {
            return "LD $arg"
        }
    }

    class St(val arg: String) : AsmOp() {
        override fun toString(): String {
            return "ST $arg"
        }
    }

    class Label(val label: String) : AsmOp() {

        override fun toString(): String {
            return "LABEL $label"
        }
    }

    class Jump(val label: String) : AsmOp() {
        override fun toString(): String {
            return "JUMP $label"
        }
    }

    class Jif(val label: String) : AsmOp() {
        override fun toString(): String {
            return "JIF $label"
        }
    }
}

private fun compile(node: StackOp): List<String> {
    val ops = ArrayList<String>()
    compile(node, ops)
    return ops
}

fun compile(nodes: List<StackOp>): List<String> {

    val ops = ArrayList<String>()
    ops += macros
    ops += externs
    ops += rodata
    val localVars = nodes.filter { it is StackOp.St }.map { (it as StackOp.St).arg }.distinct()
    ops += "SECTION .data"
    ops /= "int_read: dd 0"
    localVars.forEach { ops /= "$it: dd 0" }
    ops += prefix
    compile(nodes, ops)
    ops += suffix
    return ops
}

private fun compile(nodes: List<StackOp>, ops: MutableList<String>) {
    nodes.forEach { compile(it, ops) }
}


private fun compile(op: StackOp, ops: MutableList<String>) {
    when (op) {
        is StackOp.Read -> {
            ops /= "clib_prolog 16"
            ops /= "mov dword [esp+4], int_read"
            ops /= "mov dword [esp], format_in"
            ops /= "call _scanf"
            ops /= "clib_epilog 16"
            ops /= "push dword [int_read]"
        }
        is StackOp.Write -> { // arg is already on stack
            ops /= "pop eax"
            ops /= "clib_prolog 16"
            ops /= "mov dword [esp+4], eax"
            ops /= "mov dword [esp], format_out"
            ops /= "call _printf"
            ops /= "clib_epilog 16"
        }
        is StackOp.Nop -> {
        }
        is StackOp.Label -> {
            ops += "${op.label}:"
        }
        is StackOp.Comm -> {
        }
        is StackOp.Push -> {
            ops /= "push ${op.arg}"
        }
        is StackOp.Ld -> {
            ops /= "push dword [${op.arg}]"
        }
        is StackOp.St -> {
            ops /= "pop eax"
            ops /= "mov [${op.arg}], eax"
        }
        is StackOp.Binop -> {
            when (op.op) {
                "*" -> {
                    ops /= "pop eax"
                    ops /= "pop edx"
                    ops /= "mul edx"
                    ops /= "push eax"
                }
                "+" -> {
                    ops /= "pop eax"
                    ops /= "pop edx"
                    ops /= "add eax, edx"
                    ops /= "push eax"
                }
                "-" -> {
                    ops /= "pop edx"
                    ops /= "pop eax"
                    ops /= "sub eax, edx"
                    ops /= "push eax"
                }
                "/" -> {
                    ops /= "xor edx, edx"
                    ops /= "pop ecx"
                    ops /= "pop eax"
                    ops /= "div ecx"
                    ops /= "push eax"
                }
                "%" -> {
                    ops /= "xor edx, edx"
                    ops /= "pop ecx"
                    ops /= "pop eax"
                    ops /= "div ecx"
                    ops /= "push edx"
                }
            }
        }
        is StackOp.Jif -> {
        }
        is StackOp.Jump -> {
        }
        is StackOp.Call -> {
        }
        is StackOp.Enter -> {
        }
        is StackOp.Ret -> {
        }
    }
}

private operator fun MutableList<String>.divAssign(e: String) {
    this.add("\t$e")
}
