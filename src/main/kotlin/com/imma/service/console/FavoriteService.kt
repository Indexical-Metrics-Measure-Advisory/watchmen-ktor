package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.Favorite
import com.imma.service.Service
import com.imma.service.Services

class FavoriteService(services: Services) : Service(services) {
    fun findFavoriteById(userId: String): Favorite? {
        return persist().findById(userId, Favorite::class.java, CollectionNames.FAVORITE)
    }

    fun saveFavorite(favorite: Favorite) {
        persist().upsertOne(favorite, Favorite::class.java, CollectionNames.FAVORITE)
    }
}