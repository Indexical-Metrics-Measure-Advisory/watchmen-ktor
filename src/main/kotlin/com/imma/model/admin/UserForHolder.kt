package com.imma.model.admin

import com.imma.model.CollectionNames
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * DONOT use this entity to save user, it is created for load user list for holder
 */
@Document(collection = CollectionNames.USER)
class UserForHolder {
    @Id
    var userId: String? = null
    var name: String? = null
}