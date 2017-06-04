package ru.ifmo.ctddev.isaev

import ru.ifmo.ctddev.isaev.data.BuiltInTag
import ru.ifmo.ctddev.isaev.data.StackOp
import ru.ifmo.ctddev.isaev.data.Val
import java.util.*

val prefix = """
.globl $MAIN_NAME
    """

val rodata = """
.section .rodata
format_in: .asciz "%d"
format_out: .asciz "%d\n"
"""

val suffix = """
    movl $0, %eax
    ret
"""

private fun clib_prolog(list: MutableList<String>, size: Int){
    list /= "movl %esp, %ebx"
    list /= "andl $-16, %esp"
    list /= "subl \$$size, %esp"
    list /= "push %ebx"
    list /= "subl \$$size, %esp"
}

private fun clib_epilog(list: MutableList<String>, size: Int){
    list /= "addl \$$size, %esp"
    list /= "pop  %ebx"
    list /= "movl %ebx, %esp"
}

private fun compile(node: StackOp): List<String> {
    val ops = ArrayList<String>()
    compile(node, ops)
    return ops
}

fun compile(nodes: List<StackOp>): List<String> {
    val ops = ArrayList<String>()
    ops += rodata
    val localVars = nodes.filter { it is StackOp.St }.map { (it as StackOp.St).arg }.distinct()
    ops += ".data"
    ops /= "int_read: .int 0"
    localVars.forEach { ops /= "$it: .int 0" }
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
                    clib_prolog(ops, 16)
                    ops /= "movl \$int_read, 4(%esp)"
                    ops /= "movl format_in, %esp"
                    ops /= "call scanf"
                    clib_epilog(ops, 16)
                    ops /= "push int_read"
                }
                op.tag == BuiltInTag.WRITE -> {
                    ops /= "pop %eax"
                    clib_prolog(ops, 16)
                    ops /= "movl %eax, 4(%esp)"
                    ops /= "movl format_out, %esp"
                    ops /= "call printf"
                    clib_epilog(ops, 16)
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
            ops /= "push (${op.arg})"
        }
        is StackOp.St -> {
            ops /= "pop %eax"
            ops /= "mov %eax, (${op.arg})"
        }
        is StackOp.Binop -> {
            when (op.op) {
                "*" -> {
                    ops /= "pop %eax"
                    ops /= "pop %edx"
                    ops /= "mul %edx"
                    ops /= "push %eax"
                }
                "+" -> {
                    ops /= "pop %eax"
                    ops /= "pop %edx"
                    ops /= "add %edx, %eax"
                    ops /= "push %eax"
                }
                "-" -> {
                    ops /= "pop %edx"
                    ops /= "pop %eax"
                    ops /= "sub %edx, %eax"
                    ops /= "push %eax"
                }
                "/" -> {
                    ops /= "xor %edx, %edx"
                    ops /= "pop %ecx"
                    ops /= "pop %eax"
                    ops /= "div %ecx"
                    ops /= "push %eax"
                }
                "%" -> {
                    ops /= "xor %edx, %edx"
                    ops /= "pop %ecx"
                    ops /= "pop %eax"
                    ops /= "div %ecx"
                    ops /= "push %edx"
                }
                "<" -> {
                    ops /= "pop %edx"
                    ops /= "pop %eax"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jl $+6"
                    ops /= "push 0"
                    ops /= "jmp $+4"
                    ops /= "push 1"
                }
                "<=" -> {
                    ops /= "pop %edx"
                    ops /= "pop %eax"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jle $+6"
                    ops /= "push 0"
                    ops /= "jmp $+4"
                    ops /= "push 1"
                }
                ">" -> {
                    ops /= "pop %eax"
                    ops /= "pop %edx"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jl $+6"
                    ops /= "push 0"
                    ops /= "jmp $+4"
                    ops /= "push 1"
                }
                ">=" -> {
                    ops /= "pop %eax"
                    ops /= "pop %edx"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jle $+6"
                    ops /= "push 0"
                    ops /= "jmp $+4"
                    ops /= "push 1"
                }
                "==" -> {
                    ops /= "pop %eax"
                    ops /= "pop %edx"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "je $+6"
                    ops /= "push 0"
                    ops /= "jmp $+4"
                    ops /= "push 1"
                }
                "!=" -> {
                    ops /= "pop %eax"
                    ops /= "pop %edx"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jne $+6"
                    ops /= "push 0"
                    ops /= "jmp $+4"
                    ops /= "push 1"
                }
                else -> TODO("NOT SUPPORTED: ${op.op}")
            }
        }
        is StackOp.Jif -> {
            ops /= "pop %eax"
            ops /= "cmp 0, %eax"
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
