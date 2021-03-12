package com.imma.service.console

import com.imma.model.CollectionNames
import com.imma.model.console.Favorite
import com.imma.service.Service
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class FavoriteService(application: Application) : Service(application) {
    fun findFavoriteById(userId: String): Favorite? {
        return findFromMongo {
            it.findById(userId, Favorite::class.java, CollectionNames.FAVORITE)
        }
    }

    fun saveFavorite(favorite: Favorite) {
        writeIntoMongo {
            it.upsert(
                Query.query(Criteria.where("userId").`is`(favorite.userId)),
                Update().apply {
                    set("userId", favorite.userId)
                    set("connectedSpaceIds", favorite.connectedSpaceIds)
                    set("dashboardIds", favorite.dashboardIds)
                },
                Favorite::class.java,
                CollectionNames.FAVORITE
            )
        }
    }
}