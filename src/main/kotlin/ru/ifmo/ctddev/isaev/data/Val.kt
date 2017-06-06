package ru.ifmo.ctddev.isaev.data

/**
 * @author iisaev
 */
sealed class Val {
    class Number(val value: Int) : Val() {
        override fun copy(): Val {
            return Number(value)
        }
    }

    class Array(val content: MutableList<Val>) : Val() {
        override fun copy(): Val {
            return Array(content.map { it.copy() }.toMutableList())
        }
    }

    class Character(val value: Char) : Val() {
        override fun copy(): Val {
            return Character(value)
        }
    }

    class Str(val value: StringBuilder) : Val() {
        override fun copy(): Val {
            return Str(value.toString())
        }

        constructor(strValue: String) : this(StringBuilder(strValue))
    }

    abstract fun copy(): Val
}

val ZERO = Val.Number(0)
val ONE = Val.Number(1)