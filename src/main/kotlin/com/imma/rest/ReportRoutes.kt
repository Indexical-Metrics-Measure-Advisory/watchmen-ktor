package com.imma.rest

import com.imma.auth.Roles
import com.imma.model.console.Report
import com.imma.service.console.ReportService
import com.imma.service.console.SubjectService
import com.imma.utils.isFake
import com.imma.utils.isFakeOrNull
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.contracts.ExperimentalContracts

/**
 * @see Report.subjectId it is ignored when subjectId is given in query parameters
 * @see Report.connectId it is ignored when subjectId is given in query parameters
 */
@ExperimentalContracts
fun Route.reportSaveByMeRoute() {
    post(RouteConstants.REPORT_SAVE_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val report = call.receive<Report>()
        // get from query parameter first, or from body
        val subjectId =
            call.request.queryParameters["subject_id"].takeIf { it.isNullOrBlank() }?.apply { report.subjectId }

        val userId = report.userId
        when {
            // cannot save report which belongs to other user (at least by request data)
            !userId.isNullOrBlank() && userId != principal.name -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use report belongs to others."
            )
            // neither connect id nor report id given in request
            subjectId.isNullOrBlank() && report.reportId.isFakeOrNull() -> call.respond(
                HttpStatusCode.BadRequest,
                "Cannot create report with no subject given."
            )
            subjectId.isFake() -> call.respond(
                HttpStatusCode.BadRequest,
                "Cannot use report with fake subject appointed."
            )
            // cannot save report which belongs to other user (check with exists data)
            // connected space belongs to other user
            !subjectBelongsToCurrentUser(subjectId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use report on subject which belongs to others."
            )
            // cannot save report which belongs to other user (check with exists data)
            !reportBelongsToCurrentUser(report.reportId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use report belongs to others."
            )
            else -> {
                // assign to current authenticated user
                report.userId = principal.name

                when {
                    // subjectId not exists, use reportId to retrieve it
                    // in this case, report must be valid, it is checked in above logic already
                    subjectId.isNullOrBlank() -> {
                        // must exists, it is checked in above logic already
                        val existsReport = ReportService(application).findById(report.reportId!!)!!
                        report.connectId = existsReport.connectId
                        report.subjectId = existsReport.subjectId
                    }
                    // if subject id in query parameter exists, assign to report
                    else -> {
                        val existsSubject = SubjectService(application).findById(subjectId)!!
                        report.connectId = existsSubject.connectId
                        report.subjectId = subjectId
                    }
                }
                ReportService(application).saveReport(report)
                // remove ids when respond to client
                report.connectId = null
                report.subjectId = null
                report.userId = null
                call.respond(report)
            }
        }
    }
}

@ExperimentalContracts
fun Route.reportRenameByMeRoute() {
    get(RouteConstants.REPORT_RENAME_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val reportId = call.request.queryParameters["report_id"]
        val name = call.request.queryParameters["name"]

        when {
            reportId.isNullOrBlank() -> call.respond(HttpStatusCode.BadRequest, "Report id is required.")
            // cannot save report which belongs to other user (check with exists data)
            !reportBelongsToCurrentUser(reportId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use report belongs to others."
            )
            else -> {
                ReportService(application).renameReport(reportId, name)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

@ExperimentalContracts
fun Route.reportDeleteByMeRoute() {
    get(RouteConstants.REPORT_DELETE_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val reportId = call.request.queryParameters["report_id"]

        when {
            reportId.isNullOrBlank() -> call.respond(HttpStatusCode.BadRequest, "Report id is required.")
            // cannot save report which belongs to other user (check with exists data)
            !reportBelongsToCurrentUser(reportId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use report belongs to others."
            )
            else -> {
                ReportService(application).deleteReport(reportId)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

@ExperimentalContracts
fun Application.reportRoutes() {
    routing {
        authenticate(Roles.AUTHENTICATED.ROLE) {
            reportSaveByMeRoute()
            reportRenameByMeRoute()
            reportDeleteByMeRoute()
        }
    }
}