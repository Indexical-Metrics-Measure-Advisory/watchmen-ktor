package com.imma.model

import com.imma.utils.getCurrentDateTime
import com.imma.utils.getCurrentDateTimeAsString
import com.imma.utils.isFakeOrNull
import java.util.*
import kotlin.contracts.ExperimentalContracts

/**
 * @return true when id is null or fake one
 */
@ExperimentalContracts
fun determineFakeOrNullId(getId: () -> String?, replace: Boolean = true, setId: () -> Unit): Boolean {
    val isFakeOrNull = getId().isFakeOrNull()
    if (isFakeOrNull && replace) {
        // no id declared or it is a fake id
        setId()
    }
    return isFakeOrNull
}

private fun assignCreateTime(tuple: Tuple, force: Boolean, datetime: String = getCurrentDateTimeAsString()) {
    if (force) {
        tuple.createTime = datetime
    } else if (tuple.createTime == null || tuple.createTime?.trim()?.length == 0) {
        tuple.createTime = datetime
    }
}

private fun forceAssignCreateTime(tuple: Tuple, datetime: String = getCurrentDateTimeAsString()) {
    assignCreateTime(tuple, true, datetime)
}

private fun assignLastModifyTime(tuple: Tuple, datetimeStr: String, datetime: Date) {
    tuple.lastModifyTime = datetimeStr
    tuple.lastModified = datetime
}

private fun forceAssignModifyTime(
    tuple: Tuple,
    datetimeStr: String = getCurrentDateTimeAsString(),
    datetime: Date = getCurrentDateTime()
) {
    assignLastModifyTime(tuple, datetimeStr, datetime)
}

/**
 * force assign create time and last modify time
 */
fun forceAssignDateTimePair(tuple: Tuple) {
    forceAssignCreateTime(tuple)
    forceAssignModifyTime(tuple)
}

/**
 * assign create time, and force assign last modify time
 */
fun assignDateTimePair(tuple: Tuple) {
    assignCreateTime(tuple, false)
    forceAssignModifyTime(tuple)
}