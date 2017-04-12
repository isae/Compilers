package ru.ifmo.ctddev.isaev

import java.util.*


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
    val prefix = """
    SECTION .rodata

    SECTION .text

    extern _printf ; could also use _puts...
    extern _gets
    GLOBAL _main

    ; aligns esp to 16 bytes in preparation for calling a C library function
    ; arg is number of bytes to pad for function arguments, this should be a multiple of 16
    ; unless you are using push/pop to load args
    %macro clib_prolog 1
    mov ebx, esp        ; remember current esp
    and esp, 0xFFFFFFF0 ; align to next 16 byte boundary (could be zero offset!)
    sub esp, 12         ; skip ahead 12 so we can store original esp
    push ebx            ; store esp (16 bytes aligned again)
    sub esp, %1         ; pad for arguments (make conditional?)
    %endmacro

    ; arg must match most recent call to clib_prolog
    %macro clib_epilog 1
    add esp, %1         ; remove arg padding
    pop ebx             ; get original esp
    mov esp, ebx        ; restore
    %endmacro

    _main:"""

    val suffix = """
    mov eax, 0          ; set return code
    ret
    """
    val ops = ArrayList<String>()
    ops += prefix
    compile(nodes, ops)
    ops += suffix
    return ops
}

private fun compile(nodes: List<StackOp>, ops: MutableList<String>) {
    nodes.forEach { compile(it, ops) }
}


private fun compile(node: StackOp, ops: MutableList<String>) {
    when (node) {
        is StackOp.Read -> {
        }
        is StackOp.Write -> {
        }
        is StackOp.Nop -> {
        }
        is StackOp.Label -> {
        }
        is StackOp.Comm -> {
        }
        is StackOp.Push -> {
        }
        is StackOp.Ld -> {
        }
        is StackOp.St -> {
        }
        is StackOp.Binop -> {
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