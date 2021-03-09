package com.imma.model

import java.time.ZoneOffset
import java.util.*

open class Tuple {
    open var createTime: String? = null
    open var lastModifyTime: String? = null
    open var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time
}
