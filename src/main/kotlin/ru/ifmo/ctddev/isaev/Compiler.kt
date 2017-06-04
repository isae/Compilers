package ru.ifmo.ctddev.isaev

import ru.ifmo.ctddev.isaev.data.BuiltInTag
import ru.ifmo.ctddev.isaev.data.StackOp
import ru.ifmo.ctddev.isaev.data.Val
import java.util.*

val prefix = """
.globl _main
    """

val rodata = """
.section .rodata
format_in: .asciz "%d"
format_out: .asciz "%d\n"
"""

val fun_prefix = """
    pushl %ebp
    movl  %esp, %ebp
    andl  $-16, %esp
"""

val fun_suffix = """
    mov $1, %eax
    ret
"""

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
    ops += fun_prefix
    compile(nodes, ops)
    ops += fun_suffix
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
                    ops /= "push  \$int_read"
                    ops /= "push  format_in"
                    ops /= "call  scanf"
                }
                op.tag == BuiltInTag.WRITE -> {
                    ops /= "push format_out"
                    ops /= "call printf"
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
