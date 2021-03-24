package com.imma.service

import com.imma.persist.PersistKit
import com.imma.persist.PersistKits
import com.imma.service.admin.SpaceService
import com.imma.service.admin.UserCredentialService
import com.imma.service.admin.UserGroupService
import com.imma.service.admin.UserService
import io.ktor.application.*
import java.io.Closeable

/**
 * thread unsafe
 */
class Services(val application: Application) : Closeable {
    private val persistKits: PersistKits = PersistKits(application)

    override fun close() {
        persistKits.close()
    }

    fun application(): Application {
        return application
    }

    fun persist(): PersistKit {
        return persistKits.select()
    }

    fun <T> space(block: SpaceService.() -> T): T {
        return SpaceService(this).block()
    }

    fun <T> userGroup(block: UserGroupService.() -> T): T {
        return UserGroupService(this).block()
    }

    fun <T> userCredential(block: UserCredentialService.() -> T): T {
        return UserCredentialService(this).block()
    }

    fun <T> user(block: UserService.() -> T): T {
        return UserService(this).block()
    }
}
