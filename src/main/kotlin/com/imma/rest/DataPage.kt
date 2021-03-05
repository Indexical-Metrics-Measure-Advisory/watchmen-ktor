package com.imma.rest

data class DataPage<T>(
    var data: List<T> = mutableListOf(),
    var itemCount: Int = 0,
    var pageNumber: Int = 0,
    var pageSize: Int = 0,
    var pageCount: Int = 0
)