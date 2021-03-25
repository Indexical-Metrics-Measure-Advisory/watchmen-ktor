package com.imma.rest

import com.imma.service.Services
import com.imma.utils.isFakeOrNull
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.util.pipeline.*
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
fun PipelineContext<Unit, ApplicationCall>.connectedSpaceBelongsToCurrentUser(
    connectId: String?,
    principal: UserIdPrincipal
): Boolean {
    return when {
        // connect id is null or a fake id, not exists in persist
        // belongs to current user anyway
        connectId.isFakeOrNull() -> true
        // check persist
        else -> Services(application).use { it.connectedSpace { isConnectedSpaceBelongsTo(connectId, principal.name) } }
    }
}

@ExperimentalContracts
fun PipelineContext<Unit, ApplicationCall>.subjectBelongsToCurrentUser(
    subjectId: String?,
    principal: UserIdPrincipal
): Boolean {
    return when {
        // subject id is null or a fake id, not exists in persist
        // belongs to current user anyway
        subjectId.isFakeOrNull() -> true
        // check persist
        else -> Services(application).use { it.subject { isSubjectBelongsTo(subjectId, principal.name) } }
    }
}

@ExperimentalContracts
fun PipelineContext<Unit, ApplicationCall>.reportBelongsToCurrentUser(
    reportId: String?,
    principal: UserIdPrincipal
): Boolean {
    return when {
        // report id is null or a fake id, not exists in persist
        // belongs to current user anyway
        reportId.isFakeOrNull() -> true
        // check persist
        else -> Services(application).use { it.report { isReportBelongsTo(reportId, principal.name) } }
    }
}

@ExperimentalContracts
fun PipelineContext<Unit, ApplicationCall>.dashboardBelongsToCurrentUser(
    dashboardId: String?,
    principal: UserIdPrincipal
): Boolean {
    return when {
        // dashboard id is null or a fake id, not exists in persist
        // belongs to current user anyway
        dashboardId.isFakeOrNull() -> true
        // check persist
        else -> Services(application).use { it.dashboard { isDashboardBelongsTo(dashboardId, principal.name) } }
    }
}
