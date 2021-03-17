package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset
import java.util.*

data class EnumItem(
    var code: String? = null,
    var label: String? = null,
    var replaceCode: String? = null,
    var parentCode: String? = null
)

@Document(collection = CollectionNames.TOPIC)
data class Enum(
    @Id
    var enumId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("description")
    var description: String? = null,
    @Field("parent_enum_id")
    var parentEnumId: String? = null,
    @Transient
    var items: MutableList<EnumItem> = mutableListOf(),
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    override var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time
) : Tuple()