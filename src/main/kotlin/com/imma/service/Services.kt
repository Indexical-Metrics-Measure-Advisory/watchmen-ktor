package com.imma.service

import com.imma.persist.PersistKit
import com.imma.persist.PersistKits
import com.imma.service.admin.*
import com.imma.service.console.*
import com.imma.service.login.LoginService
import java.io.Closeable

/**
 * thread unsafe
 */
class Services : Closeable {
    private val persistKits: PersistKits = PersistKits()

    override fun close() {
        persistKits.close()
    }

    fun persist(): PersistKit {
        return persistKits.select()
    }

    fun <T> auth(block: LoginService.() -> T): T {
        return LoginService(this).block()
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

    fun <T> connectedSpaceGraphics(block: ConnectedSpaceGraphicsService.() -> T): T {
        return ConnectedSpaceGraphicsService(this).block()
    }

    fun <T> connectedSpace(block: ConnectedSpaceService.() -> T): T {
        return ConnectedSpaceService(this).block()
    }

    fun <T> dashboard(block: DashboardService.() -> T): T {
        return DashboardService(this).block()
    }

    fun <T> favorite(block: FavoriteService.() -> T): T {
        return FavoriteService(this).block()
    }

    fun <T> lastSnapshot(block: LastSnapshotService.() -> T): T {
        return LastSnapshotService(this).block()
    }

    fun <T> report(block: ReportService.() -> T): T {
        return ReportService(this).block()
    }

    fun <T> subject(block: SubjectService.() -> T): T {
        return SubjectService(this).block()
    }

    fun <T> enumeration(block: EnumService.() -> T): T {
        return EnumService(this).block()
    }

    fun <T> pipelineGraphics(block: PipelineGraphicsService.() -> T): T {
        return PipelineGraphicsService(this).block()
    }

    fun <T> pipeline(block: PipelineService.() -> T): T {
        return PipelineService(this).block()
    }

    fun <T> topic(block: TopicService.() -> T): T {
        return TopicService(this).block()
    }
}
