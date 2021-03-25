package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.Favorite
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.Service
import com.imma.service.Services

class FavoriteService(services: Services) : Service(services) {
    fun findFavoriteById(userId: String): Favorite? {
        return persist().findById(userId, Favorite::class.java, CollectionNames.FAVORITE)
    }

    fun saveFavorite(favorite: Favorite) {
        persist().upsert(
            where {
                column("userId") eq favorite.userId
            },
            update {
                set("userId") to favorite.userId
                set("connectedSpaceIds") to favorite.connectedSpaceIds
                set("dashboardIds") to favorite.dashboardIds
            },
            Favorite::class.java, CollectionNames.FAVORITE
        )
    }
}