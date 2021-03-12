package com.imma.model.admin

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset.UTC
import java.util.*

@Document(collection = CollectionNames.USER_CREDENTIAL)
data class UserCredential(
    @Id
    var userId: String? = null,
    @Indexed(unique = true)
    @Field("name")
    var name: String? = null,
    @Field("credential")
    var credential: String? = null,
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    override var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(UTC)).time
) : Tuple()
