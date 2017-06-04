package ru.ifmo.ctddev.isaev.data

/**
 * @author iisaev
 */

enum class BuiltInTag(val argSize: Int) {
    READ(0), WRITE(1), STRLEN(1),
    STRGET(2), STRSET(3), STRSUB(3),
    STRDUP(1), STRCAT(2), STRCMP(2),
    STRMAKE(2), ARRMAKE(2), ARRLEN(1)
}