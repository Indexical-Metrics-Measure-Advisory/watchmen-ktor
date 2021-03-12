package com.imma.utils

import com.imma.model.page.DataPage
import com.imma.model.page.Pageable

fun <T> findPageData(count: Long, find: () -> List<T>): List<T> {
    val items: List<T>
    if (count == 0L) {
        items = mutableListOf()
    } else {
        items = find()
    }
    return items
}

private fun computePageCount(count: Long, pageSize: Int): Int {
    return (count / pageSize).toInt() + 1
}

fun <T> toDataPage(data: List<T>, count: Long, pageable: Pageable): DataPage<T> {
    val pageCount = computePageCount(count, pageable.pageSize)

    return DataPage<T>(data, count.toInt(), pageable.pageNumber, pageable.pageSize, pageCount)
}