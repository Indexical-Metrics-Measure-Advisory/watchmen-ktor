package com.imma.utils

fun String.isFakeId(): Boolean {
    return this.startsWith("f-")
}