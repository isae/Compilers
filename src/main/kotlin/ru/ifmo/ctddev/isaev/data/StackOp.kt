package ru.ifmo.ctddev.isaev.data

sealed class StackOp {
    class BuiltIn(val tag: BuiltInTag) : StackOp() {
        override fun toString(): String {
            return tag.toString()
        }
    }

    class Nop : StackOp() {
        override fun toString(): String {
            return "NOP"
        }
    }

    class Push(val arg: Val) : StackOp() {
        override fun toString(): String {
            return "PUSH $arg"
        }
    }

    class Ld(val arg: String) : StackOp() {
        override fun toString(): String {
            return "LD $arg"
        }
    }

    class LdArr(val arg: String, val indexes: Int) : StackOp() {
        override fun toString(): String {
            return "LD_ARR $arg $indexes"
        }
    }

    class MakeArr(val size: Int) : StackOp() {
        override fun toString(): String {
            return "MAKE_ARR $size"
        }
    }

    class St(val arg: String) : StackOp() {
        override fun toString(): String {
            return "ST $arg"
        }
    }

    class StArr(val arg: String, val indexes: Int) : StackOp() {
        override fun toString(): String {
            return "ST_ARR $arg $indexes"
        }
    }

    class Binop(val op: String) : StackOp() {
        override fun toString(): String {
            return "BINOP $op"
        }
    }

    class Label(val label: String) : StackOp() {

        override fun toString(): String {
            return "LABEL $label"
        }
    }

    class Jump(val label: String) : StackOp() {
        override fun toString(): String {
            return "JUMP $label"
        }
    }

    class Jif(val label: String) : StackOp() {
        override fun toString(): String {
            return "JIF $label"
        }
    }

    class Comm(val comment: String) : StackOp() {
        override fun toString(): String {
            return "COMM \"$comment\""
        }
    }

    class Call(val funName: String, val argsSize: Int) : StackOp() {
        override fun toString(): String {
            return "CALL $funName $argsSize"
        }
    }

    class Enter(val argNames: List<String>, val localVariables: Set<String>) : StackOp() {
        override fun toString(): String {
            return "ENTER $argNames $localVariables"
        }
    }

    class Ret : StackOp() {
        override fun toString(): String {
            return "RET"
        }
    }
}