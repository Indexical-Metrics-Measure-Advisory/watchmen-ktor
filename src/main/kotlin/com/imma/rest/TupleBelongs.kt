package com.imma.rest

import com.imma.service.Services
import com.imma.utils.isFakeOrNull
import io.ktor.auth.*
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
fun connectedSpaceBelongsToCurrentUser(
	connectId: String?,
	principal: UserIdPrincipal
): Boolean {
	return when {
		// connect id is null or a fake id, not exists in persist
		// belongs to current user anyway
		connectId.isFakeOrNull() -> true
		// check persist
		else -> Services().use { it.connectedSpace { isConnectedSpaceBelongsTo(connectId, principal.name) } }
	}
}

@ExperimentalContracts
fun subjectBelongsToCurrentUser(
	subjectId: String?,
	principal: UserIdPrincipal
): Boolean {
	return when {
		// subject id is null or a fake id, not exists in persist
		// belongs to current user anyway
		subjectId.isFakeOrNull() -> true
		// check persist
		else -> Services().use { it.subject { isSubjectBelongsTo(subjectId, principal.name) } }
	}
}

@ExperimentalContracts
fun reportBelongsToCurrentUser(
	reportId: String?,
	principal: UserIdPrincipal
): Boolean {
	return when {
		// report id is null or a fake id, not exists in persist
		// belongs to current user anyway
		reportId.isFakeOrNull() -> true
		// check persist
		else -> Services().use { it.report { isReportBelongsTo(reportId, principal.name) } }
	}
}

@ExperimentalContracts
fun dashboardBelongsToCurrentUser(
	dashboardId: String?,
	principal: UserIdPrincipal
): Boolean {
	return when {
		// dashboard id is null or a fake id, not exists in persist
		// belongs to current user anyway
		dashboardId.isFakeOrNull() -> true
		// check persist
		else -> Services().use { it.dashboard { isDashboardBelongsTo(dashboardId, principal.name) } }
	}
}
