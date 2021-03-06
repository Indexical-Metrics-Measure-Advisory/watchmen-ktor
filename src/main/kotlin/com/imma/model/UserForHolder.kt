package com.imma.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * DONOT use this entity to save user, it is created for load user list for holder
 */
@Document(collection = "user")
class UserForHolder {
    @Id
    var userId: String? = null
    var name: String? = null
}