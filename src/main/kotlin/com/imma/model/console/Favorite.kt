package com.imma.model.console

import com.imma.model.CollectionNames
import com.imma.persist.annotation.Entity
import com.imma.persist.annotation.Field
import com.imma.persist.annotation.Id

@Entity(CollectionNames.FAVORITE)
class Favorite(
    @Id
    var userId: String? = null,
    @Field("connected_space_ids")
    var connectedSpaceIds: List<String>? = mutableListOf(),
    @Field("dashboard_ids")
    var dashboardIds: List<String>? = mutableListOf()
)