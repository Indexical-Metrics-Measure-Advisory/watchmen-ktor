package com.imma.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * DONOT use this entity to save user group, it is created for load user group list for holder
 */
@Document(collection = CollectionNames.USER_GROUP)
class UserGroupForHolder {
    @Id
    var userGroupId: String? = null
    var name: String? = null
}