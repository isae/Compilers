package ru.ifmo.ctddev.isaev

import ru.ifmo.ctddev.isaev.data.BuiltInTag
import ru.ifmo.ctddev.isaev.data.Val
import java.io.BufferedReader
import java.io.PrintWriter

fun apply(l: Val, r: Val, op: String): Int {
    if (listOf("+", "-", "*", "&", "|", "/", "%").contains(op)) {
        return applyForInt(takeInt(l), takeInt(r), op)
    } else {
        if (l is Val.Character && r is Val.Character) {
            return applyForChar(l.value, r.value, op)
        } else if (l is Val.Str && r is Val.Str) {
            return applyForString(l.value.toString(), r.value.toString(), op)
        } else if (l is Val.Number && r is Val.Number) {
            return applyForInt(l.value, r.value, op)
        } else {
            throw IllegalStateException("Apply of operation $op is not supported for ${l::class.simpleName} and ${r::class.simpleName}")
        }
    }
}

fun applyForInt(l: Int, r: Int, op: String): Int {
    return when (op) {
        "+" -> l + r
        "-" -> l - r
        "*" -> l * r
        "/" -> l / r
        "%" -> l % r
        "&" -> if (l != 0 && r != 0) 1 else 0
        "|" -> if (l == 0 && r == 0) 0 else 1
        "!!" -> if ((l == 0) == (r == 0)) 0 else 1
        "==" -> if (l == r) 1 else 0
        "!=" -> if (l != r) 1 else 0
        ">" -> if (l > r) 1 else 0
        ">=" -> if (l >= r) 1 else 0
        "<" -> if (l < r) 1 else 0
        "<=" -> if (l <= r) 1 else 0
        else -> TODO("Invalid operation $op for $l and $r")
    }
}

fun applyForString(l: String, r: String, op: String): Int {
    return when (op) {
        "==" -> if (l == r) 1 else 0
        "!=" -> if (l != r) 1 else 0
        ">" -> if (l > r) 1 else 0
        ">=" -> if (l >= r) 1 else 0
        "<" -> if (l < r) 1 else 0
        "<=" -> if (l <= r) 1 else 0
        else -> TODO("Invalid operation $op for $l and $r")
    }
}

fun applyForChar(l: Char, r: Char, op: String): Int {
    return when (op) {
        "==" -> if (l == r) 1 else 0
        "!=" -> if (l != r) 1 else 0
        ">" -> if (l > r) 1 else 0
        ">=" -> if (l >= r) 1 else 0
        "<" -> if (l < r) 1 else 0
        "<=" -> if (l <= r) 1 else 0
        else -> TODO("Invalid operation $op for $l and $r")
    }
}


var read_count: Int = 0
var write_count: Int = 0

fun init() { //TODO: remove this shitty hack
    read_count = 0
    write_count = 0
}

fun printPrefix(): String {
    return if (write_count > 0) "" else ("> ".repeat(read_count))
}

fun builtInRead(reader: BufferedReader): Val {
    ++read_count
    val str = reader.readLine()
    try {
        return Val.Number(str!!.toInt())
    } catch (e: NumberFormatException) {
        return if (str.length == 1) {
            Val.Character(str[0])
        } else {
            Val.Str(str)
        }
    }
}

fun takeInt(arg: Val): Int {
    if (arg is Val.Number) {
        return arg.value
    } else {
        throw IllegalStateException("Type mismatch: expected Int, found ${arg::class.simpleName}")
    }
}

fun takeString(arg: Val): StringBuilder {
    if (arg is Val.Str) {
        return arg.value
    } else {
        throw IllegalStateException("Type mismatch: expected String, found ${arg::class.simpleName}")
    }
}

fun takeChar(arg: Val): Char {
    if (arg is Val.Character) {
        return arg.value
    } else {
        throw IllegalStateException("Type mismatch: expected Char, found ${arg::class.simpleName}")
    }
}

fun assertArgNumber(functionName: String, expected: Int, actual: Int) {
    if (expected != actual) {
        throw IllegalStateException("Expected $expected args for function $functionName, but found $actual")
    }
}

fun builtInWrite(arg: Val, writer: PrintWriter): Unit {
    val value = when (arg) {
        is Val.Number -> arg.value
        is Val.Str -> arg.value
        is Val.Character -> arg.value.toInt()
        else -> throw IllegalStateException("${arg::class.simpleName} has no value")
    }
    writer.println(printPrefix() + value)
    writer.flush()
    ++write_count
}

fun performBuiltIn(tag: BuiltInTag, reader: BufferedReader, writer: PrintWriter, args: List<Val>): Val {
    assertArgNumber(tag.toString(), tag.argSize, args.size)
    return when (tag) {
        BuiltInTag.READ -> builtInRead(reader)
        BuiltInTag.WRITE -> {
            args.forEach { builtInWrite(it, writer) }
            return Val.Void()
        }
        BuiltInTag.STRLEN -> {
            val value = takeString(args[0])
            return Val.Number(value.length)
        }
        BuiltInTag.STRGET -> {
            val str = takeString(args[0])
            val index = takeInt(args[1])
            return Val.Character(str[index])
        }
        BuiltInTag.STRSET -> {
            val str = takeString(args[0])
            val index = takeInt(args[1])
            val char = takeChar(args[2])
            str[index] = char
            return Val.Void()
        }
        BuiltInTag.STRSUB -> {
            val str = takeString(args[0])
            val from = takeInt(args[1])
            val length = takeInt(args[2])
            return Val.Str(str.substring(from, from + length))
        }
        BuiltInTag.STRDUP -> {
            return Val.Str(takeString(args[0]).toString())
        }
        BuiltInTag.STRCAT -> {
            val fst = takeString(args[0]).toString()
            val snd = takeString(args[1]).toString()
            return Val.Str(fst + snd)
        }
        BuiltInTag.STRCMP -> {
            val fst = takeString(args[0]).toString()
            val snd = takeString(args[1]).toString()
            return Val.Number(fst.compareTo(snd))
        }
        BuiltInTag.STRMAKE -> {
            val numberOfChars = takeInt(args[0])
            val char = takeChar(args[1])
            return Val.Str(char.toString().repeat(numberOfChars))
        }
        BuiltInTag.ARRMAKE -> {
            val size = takeInt(args[0])
            val value = args[1]
            val result = ArrayList<Val>()
            repeat(size, {
                result.add(value.copy())
            })
            return Val.Array(result)
        }
        BuiltInTag.ARRLEN -> {
            val value = args[0]
            val array = value as? Val.Array ?: throw IllegalStateException("An argument of arrlen must be an array; found ${value::class.simpleName}")
            return Val.Number(array.content.size)
        }
    }
}