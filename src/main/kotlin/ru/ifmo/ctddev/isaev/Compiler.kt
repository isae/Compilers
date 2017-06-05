package ru.ifmo.ctddev.isaev

import ru.ifmo.ctddev.isaev.data.BuiltInTag
import ru.ifmo.ctddev.isaev.data.StackOp
import ru.ifmo.ctddev.isaev.data.Val
import java.util.*

val prefix = """
read_int:
  enter $16, $0
  andl $-16, %esp
  lea int_read, %eax
  movl %eax, 4(%esp)
  lea format_in, %eax
  movl %eax, (%esp)
  call scanf
  movl $0, %eax
  leave
  ret
  
write_int:
  popl %edx
  popl %eax
  pushl %edx
  enter $16, $0
  andl $-16, %esp
  movl %eax, 4(%esp)
  lea format_out, %eax
  movl %eax, (%esp)
  call printf
  movl $0, %eax
  leave
  ret

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
                    ops /= "call read_int"
                    ops /= "push int_read"
                }
                op.tag == BuiltInTag.WRITE -> {
                    ops /= "call write_int"
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
            is Val.Number -> ops /= "pushl \$${op.arg.value}"
            else -> TODO("Only Integer are implemented for now")
        }
        is StackOp.Ld -> {
            ops /= "pushl (${op.arg})"
        }
        is StackOp.St -> {
            ops /= "popl %eax"
            ops /= "movl %eax, (${op.arg})"
        }
        is StackOp.Binop -> {
            when (op.op) {
                "*" -> {
                    ops /= "popl %eax"
                    ops /= "popl %edx"
                    ops /= "mull %edx"
                    ops /= "pushl %eax"
                }
                "+" -> {
                    ops /= "popl %eax"
                    ops /= "popl %edx"
                    ops /= "addl %edx, %eax"
                    ops /= "pushl %eax"
                }
                "-" -> {
                    ops /= "popl %edx"
                    ops /= "popl %eax"
                    ops /= "subl %edx, %eax"
                    ops /= "pushl %eax"
                }
                "/" -> {
                    ops /= "xorl %edx, %edx"
                    ops /= "popl %ecx"
                    ops /= "popl %eax"
                    ops /= "divl %ecx"
                    ops /= "pushl %eax"
                }
                "%" -> {
                    ops /= "xorl %edx, %edx"
                    ops /= "popl %ecx"
                    ops /= "popl %eax"
                    ops /= "divl %ecx"
                    ops /= "pushl %edx"
                }
                "<" -> {
                    ops /= "popl %edx"
                    ops /= "popl %eax"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jl $+6"
                    ops /= "pushl 0"
                    ops /= "jmp $+4"
                    ops /= "pushl 1"
                }
                "<=" -> {
                    ops /= "popl %edx"
                    ops /= "popl %eax"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jle $+6"
                    ops /= "pushl 0"
                    ops /= "jmp $+4"
                    ops /= "pushl 1"
                }
                ">" -> {
                    ops /= "popl %eax"
                    ops /= "popl %edx"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jl $+6"
                    ops /= "pushl 0"
                    ops /= "jmp $+4"
                    ops /= "pushl 1"
                }
                ">=" -> {
                    ops /= "popl %eax"
                    ops /= "popl %edx"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jle $+6"
                    ops /= "pushl 0"
                    ops /= "jmp $+4"
                    ops /= "pushl 1"
                }
                "==" -> {
                    ops /= "popl %eax"
                    ops /= "popl %edx"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "je $+6"
                    ops /= "pushl 0"
                    ops /= "jmp $+4"
                    ops /= "pushl 1"
                }
                "!=" -> {
                    ops /= "popl %eax"
                    ops /= "popl %edx"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jne $+6"
                    ops /= "pushl 0"
                    ops /= "jmp $+4"
                    ops /= "pushl 1"
                }
                else -> TODO("NOT SUPPORTED: ${op.op}")
            }
        }
        is StackOp.Jif -> {
            ops /= "popl %eax"
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
