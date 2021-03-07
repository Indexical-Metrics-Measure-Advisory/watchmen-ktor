package com.imma.model

import com.imma.utils.getCurrentDateTime
import com.imma.utils.isFakeId

fun determineFakeId(getId: () -> String?, replace: Boolean = true, setId: () -> Unit): Boolean {
    val isFakeId = getId()?.isFakeId()
    val fake = isFakeId == true || isFakeId == null
    if (fake && replace) {
        // no id declared or it is a fake id
        setId()
    }
    return fake
}

fun assignCreateTime(tuple: Tuple, force: Boolean, datetime: String = getCurrentDateTime()) {
    if (force) {
        tuple.createTime = datetime
    } else if (tuple.createTime == null || tuple.createTime?.trim()?.length == 0) {
        tuple.createTime = datetime
    }
}

fun forceAssignCreateTime(tuple: Tuple, datetime: String = getCurrentDateTime()) {
    assignCreateTime(tuple, true, datetime)
}

fun assignLastModifyTime(tuple: Tuple, force: Boolean, datetime: String = getCurrentDateTime()) {
    if (force) {
        tuple.lastModifyTime = datetime
    } else if (tuple.lastModifyTime == null || tuple.lastModifyTime?.trim()?.length == 0) {
        tuple.lastModifyTime = datetime
    }
}

fun forceAssignModifyTime(tuple: Tuple, datetime: String = getCurrentDateTime()) {
    assignLastModifyTime(tuple, true, datetime)
}

/**
 * force assign create time and last modify time
 */
fun forceAssignDateTimePair(tuple: Tuple, datetime: String = getCurrentDateTime()) {
    forceAssignCreateTime(tuple, datetime)
    forceAssignModifyTime(tuple, datetime)
}

/**
 * assign create time, and force assign last modify time
 */
fun assignDateTimePair(tuple: Tuple, datetime: String = getCurrentDateTime()) {
    assignCreateTime(tuple, false, datetime)
    forceAssignModifyTime(tuple, datetime)
}