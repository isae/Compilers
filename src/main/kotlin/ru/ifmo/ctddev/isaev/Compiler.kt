package ru.ifmo.ctddev.isaev

import ru.ifmo.ctddev.isaev.data.BuiltInTag
import ru.ifmo.ctddev.isaev.data.StackOp
import ru.ifmo.ctddev.isaev.data.Val
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
extern printf
extern scanf
"""
val prefix = """
SECTION .text
GLOBAL main
    """

val rodata = """
SECTION .rodata
format_in: db "%d", 0
format_out: db "%d", 10, 0
"""

val suffix = """
    mov eax, 1
    ret
"""

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
        is StackOp.BuiltIn -> {
            when {
                op.tag == BuiltInTag.READ -> {
                    ops /= "clib_prolog 16"
                    ops /= "mov dword [esp+4], int_read"
                    ops /= "mov dword [esp], format_in"
                    ops /= "call scanf"
                    ops /= "clib_epilog 16"
                    ops /= "push dword [int_read]"
                }
                op.tag == BuiltInTag.WRITE -> {
                    ops /= "pop eax"
                    ops /= "clib_prolog 16"
                    ops /= "mov dword [esp+4], eax"
                    ops /= "mov dword [esp], format_out"
                    ops /= "call printf"
                    ops /= "clib_epilog 16"
                }
                else -> TODO("Not implemented")
            }

        }
        is StackOp.Nop -> {
        }
        is StackOp.Label -> {
            ops += "${op.label}:"
        }
        is StackOp.Comm -> {
            ops /= ";${op.comment}"
        }
        is StackOp.Push -> when (op.arg) {
            is Val.Number -> ops /= "push ${op.arg.value}"
            else -> TODO("Only Integer are implemented for now")
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
                "<" -> {
                    ops /= "pop edx"
                    ops /= "pop eax"
                    ops /= "cmp eax, edx" //compare and set flags
                    ops /= "jl $+6"
                    ops /= "push 0"
                    ops /= "jmp $+4"
                    ops /= "push 1"
                }
                "<=" -> {
                    ops /= "pop edx"
                    ops /= "pop eax"
                    ops /= "cmp eax, edx" //compare and set flags
                    ops /= "jle $+6"
                    ops /= "push 0"
                    ops /= "jmp $+4"
                    ops /= "push 1"
                }
                ">" -> {
                    ops /= "pop eax"
                    ops /= "pop edx"
                    ops /= "cmp eax, edx" //compare and set flags
                    ops /= "jl $+6"
                    ops /= "push 0"
                    ops /= "jmp $+4"
                    ops /= "push 1"
                }
                ">=" -> {
                    ops /= "pop eax"
                    ops /= "pop edx"
                    ops /= "cmp eax, edx" //compare and set flags
                    ops /= "jle $+6"
                    ops /= "push 0"
                    ops /= "jmp $+4"
                    ops /= "push 1"
                }
                "==" -> {
                    ops /= "pop eax"
                    ops /= "pop edx"
                    ops /= "cmp eax, edx" //compare and set flags
                    ops /= "je $+6"
                    ops /= "push 0"
                    ops /= "jmp $+4"
                    ops /= "push 1"
                }
                "!=" -> {
                    ops /= "pop eax"
                    ops /= "pop edx"
                    ops /= "cmp eax, edx" //compare and set flags
                    ops /= "jne $+6"
                    ops /= "push 0"
                    ops /= "jmp $+4"
                    ops /= "push 1"
                }
                else -> TODO("NOT SUPPORTED: ${op.op}")
            }
        }
        is StackOp.Jif -> {
            ops /= "pop eax"
            ops /= "cmp eax, 0"
            ops /= "jne $+4"
            ops /= "jmp ${op.label}"
        }
        is StackOp.Jump -> {
            ops /= "jmp ${op.label}"
        }
        is StackOp.Call -> {
            TODO("NOT SUPPORTED")
        }
        is StackOp.Enter -> {
            TODO("NOT SUPPORTED")
        }
        is StackOp.Ret -> {
            TODO("NOT SUPPORTED")
        }
    }
}

private operator fun MutableList<String>.divAssign(e: String) {
    this.add("\t$e")
}
