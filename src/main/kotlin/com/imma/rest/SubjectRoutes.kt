package com.imma.rest

import com.imma.auth.Roles
import com.imma.model.console.Subject
import com.imma.service.Services
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
 * @see Subject.connectId it is ignored when connectId is given in query parameters
 */
@ExperimentalContracts
fun Route.subjectSaveByMeRoute() {
    post(RouteConstants.SUBJECT_SAVE_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val subject = call.receive<Subject>()
        // get from query parameter first, or from body
        val connectId = call.request.queryParameters["connect_id"]
            .takeIf { it.isNullOrBlank() }?.apply { subject.connectId }

        val userId = subject.userId
        when {
            // cannot save subject which belongs to other user (at least by request data)
            !userId.isNullOrBlank() && userId != principal.name -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use subject belongs to others."
            )
            // neither connect id nor subject id given in request
            connectId.isNullOrBlank() && subject.subjectId.isFakeOrNull() -> call.respond(
                HttpStatusCode.BadRequest,
                "Cannot create subject with no connected space given."
            )
            connectId.isFake() -> call.respond(
                HttpStatusCode.BadRequest,
                "Cannot use subject with fake connected space appointed."
            )
            // cannot save subject which belongs to other user (check with exists data)
            // connected space belongs to other user
            !connectedSpaceBelongsToCurrentUser(connectId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use subject on connected space which belongs to others."
            )
            // cannot save subject which belongs to other user (check with exists data)
            !subjectBelongsToCurrentUser(subject.subjectId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use subject belongs to others."
            )
            else -> {
                // assign to current authenticated user
                subject.userId = principal.name

                when {
                    // connectId not exists, use subjectId to retrieve it
                    // in this case, subject must be valid, it is checked in above logic already
                    connectId.isNullOrBlank() -> {
                        // must exists, it is checked in above logic already
                        val existsSubject =
                            Services().use { it.subject { findSubjectById(subject.subjectId!!)!! } }
                        subject.connectId = existsSubject.connectId
                    }
                    // if connect id in query parameter exists, assign to subject
                    else -> subject.connectId = connectId
                }
                Services().use { it.subject { saveSubject(subject) } }
                // remove ids when respond to client
                subject.connectId = null
                subject.userId = null
                call.respond(subject)
            }
        }
    }
}

@ExperimentalContracts
fun Route.subjectRenameByMeRoute() {
    get(RouteConstants.SUBJECT_RENAME_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val subjectId = call.request.queryParameters["subject_id"]
        val name = call.request.queryParameters["name"]

        when {
            subjectId.isNullOrBlank() -> call.respond(HttpStatusCode.BadRequest, "Subject id is required.")
            // cannot save subject which belongs to other user (check with exists data)
            !subjectBelongsToCurrentUser(subjectId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use subject belongs to others."
            )
            else -> {
                Services().use { it.subject { renameSubject(subjectId, name) } }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

@ExperimentalContracts
fun Route.subjectDeleteByMeRoute() {
    get(RouteConstants.SUBJECT_DELETE_BY_ME) {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        val subjectId = call.request.queryParameters["subject_id"]

        when {
            subjectId.isNullOrBlank() -> call.respond(HttpStatusCode.BadRequest, "Subject id is required.")
            // cannot save subject which belongs to other user (check with exists data)
            !subjectBelongsToCurrentUser(subjectId, principal) -> call.respond(
                HttpStatusCode.Forbidden,
                "Cannot use subject belongs to others."
            )
            else -> {
                Services().use { it.subject { deleteSubject(subjectId) } }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

@ExperimentalContracts
fun Application.subjectRoutes() {
    routing {
        authenticate(Roles.AUTHENTICATED.ROLE) {
            subjectSaveByMeRoute()
            subjectRenameByMeRoute()
            subjectDeleteByMeRoute()
        }
    }
}