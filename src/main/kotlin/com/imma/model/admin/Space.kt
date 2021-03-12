package com.imma.model.admin

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset
import java.util.*

@Document(collection = CollectionNames.SPACE)
data class Space(
    @Id
    var spaceId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("description")
    var description: String? = null,
    @Field("topic_ids")
    var topicIds: List<String>? = mutableListOf(),
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    override var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time
) : Tuple() {
    // transient fields here for avoid construct exception by spring data using kotlin data class
    @Transient
    var groupIds: List<String>? = mutableListOf()
}