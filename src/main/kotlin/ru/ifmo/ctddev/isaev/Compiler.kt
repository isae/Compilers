package ru.ifmo.ctddev.isaev

import ru.ifmo.ctddev.isaev.data.BuiltInTag
import ru.ifmo.ctddev.isaev.data.StackOp
import ru.ifmo.ctddev.isaev.data.Val
import java.util.*
import kotlin.collections.HashMap

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
  enter $16, $0
  andl $-16, %esp
  movl 8(%ebp), %eax
  movl %eax, 4(%esp)
  lea format_out, %eax
  movl %eax, (%esp)
  call printf
  movl $0, %eax
  leave
  ret

debug_int:
  enter $16, $0
  andl $-16, %esp
  movl 8(%ebp), %eax
  movl %eax, 4(%esp)
  lea debug_out, %eax
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
debug_out: .asciz "Return value: %d\n"
"""

val suffix = """
movl $0, %eax
ret
"""

val COMM_PREFIX = "//"
val debugEnabled = true


fun compile(nodes: List<StackOp>): List<String> {
    val ops = ArrayList<String>()
    ops += rodata
    ops += ".data"
    ops /= "int_read: .int 0"
    ops += prefix
    compile(nodes, ops)
    ops += suffix
    return ops
}

private fun compile(nodes: List<StackOp>, ops: MutableList<String>) {
    val varOffsets = HashMap<String, Int>()
    nodes.forEach { compile(it, ops, varOffsets) }
}

private fun compile(op: StackOp, ops: MutableList<String>, varOffsets: HashMap<String, Int>) {
    when (op) {
        is StackOp.BuiltIn -> {
            when {
                op.tag == BuiltInTag.READ -> {
                    ops /= "call read_int"
                    ops /= "push int_read"
                }
                op.tag == BuiltInTag.WRITE -> {
                    ops /= "call write_int"
                    ops /= "popl %eax // Value is still on stack after write"
                }
                else -> TODO("Not implemented")
            }

        }
        is StackOp.Nop -> {
        }
        is StackOp.Label -> {
            ops += "${op.label}:"
            if (op.label == MAIN_NAME) {
                ops += "xorl %ecx, %ecx"
            }
        }
        is StackOp.Comm -> {
            ops /= "$COMM_PREFIX${op.comment}"
        }
        is StackOp.Push -> when (op.arg) {
            is Val.Number -> ops /= "pushl \$${op.arg.value}"
            else -> TODO("Only Integer are implemented for now")
        }
        is StackOp.Ld -> {
            val offset = varOffsets[op.arg] ?: throw IllegalStateException("Variable not defined: ${op.arg}")
            ops /= "pushl $offset(%ebp) // Load ${op.arg}"
        }
        is StackOp.St -> {
            ops /= "popl %eax"
            val offset = varOffsets[op.arg] ?: throw IllegalStateException("Variable not defined: ${op.arg}")
            ops /= "movl %eax, $offset(%ebp) // Store ${op.arg}"
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
                    ops /= "jl .+6"
                    ops /= "pushl \$0"
                    ops /= "jmp .+4"
                    ops /= "pushl \$1"
                }
                "<=" -> {
                    ops /= "popl %edx"
                    ops /= "popl %eax"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jle .+6"
                    ops /= "pushl \$0"
                    ops /= "jmp .+4"
                    ops /= "pushl \$1"
                }
                ">" -> {
                    ops /= "popl %eax"
                    ops /= "popl %edx"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jl .+6"
                    ops /= "pushl \$0"
                    ops /= "jmp .+4"
                    ops /= "pushl \$1"
                }
                ">=" -> {
                    ops /= "popl %eax"
                    ops /= "popl %edx"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jle .+6"
                    ops /= "pushl \$0"
                    ops /= "jmp .+4"
                    ops /= "pushl \$1"
                }
                "==" -> {
                    ops /= "popl %eax"
                    ops /= "popl %edx"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "je .+6"
                    ops /= "pushl \$0"
                    ops /= "jmp .+4"
                    ops /= "pushl \$1"
                }
                "!=" -> {
                    ops /= "popl %eax"
                    ops /= "popl %edx"
                    ops /= "cmp %edx, %eax" //compare and set flags
                    ops /= "jne .+6"
                    ops /= "pushl \$0"
                    ops /= "jmp .+4"
                    ops /= "pushl \$1"
                }
                else -> TODO("NOT SUPPORTED: ${op.op}")
            }
        }
        is StackOp.Jif -> {
            ops /= "popl %eax"
            ops /= "cmp $0, %eax"
            ops /= "je ${op.label}"
        }
        is StackOp.Jump -> {
            ops /= "jmp ${op.label}"
        }
        is StackOp.Call -> {
            ops /= "call ${op.funName}"
            ops /= "pushl %eax"
        }
        is StackOp.Enter -> {
            varOffsets.clear()
            var argOffset = 8
            op.argNames.forEach {
                varOffsets[it] = argOffset
                argOffset += 4
            }
            var localVarOffset = -4
            op.localVariables.reversed().forEach {
                varOffsets[it] = localVarOffset
                localVarOffset -= 4
            }
            val frameSize = (op.localVariables.size + 1) * 4
            ops /= "enter \$$frameSize, $0"
        }
        is StackOp.Ret -> {
            if (debugEnabled) {
                ops /= "call debug_int // Debug return value that is on stack"
            }
            ops /= "popl %eax"
            ops /= "leave"
            ops /= "ret\n"
        }
    }
}

private operator fun MutableList<String>.divAssign(e: String) {
    this.add("\t$e")
}
