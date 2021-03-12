package com.imma.model.console

import com.imma.model.CollectionNames
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = CollectionNames.FAVORITE)
class Favorite(
    @Id
    var userId: String? = null,
    @Field("connected_space_ids")
    var connectedSpaceIds: List<String>? = mutableListOf(),
    @Field("dashboard_ids")
    var dashboardIds: List<String>? = mutableListOf()
)