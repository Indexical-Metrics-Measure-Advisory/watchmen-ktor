package com.imma.utils

import java.text.SimpleDateFormat
import java.util.*

fun getCurrentDateTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
}
