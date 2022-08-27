package world.komq.loneliness

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import world.komq.loneliness.FetchPaperAPI.fetchBuilds
import world.komq.loneliness.FetchPaperAPI.fetchDownload
import world.komq.loneliness.FetchPaperAPI.fetchLatestArtifact
import world.komq.loneliness.FetchPaperAPI.fetchProjects
import world.komq.loneliness.FetchPaperAPI.fetchVersions

val projects = listOf("paper", "velocity", "waterfall")

fun main() {
    embeddedServer(Netty, port = 25534) {
        install(IgnoreTrailingSlash)
        routing {
            get("/") {
                if (checkCurl(call) == false) call.respondText("Lonely Intelligence.") else call.respondNoCurl()
            }
            projects.forEach { project ->
                get("/$project") {
                    if (checkCurl(call) == false) call.respondRedirect(fetchLatestArtifact(project), true) else call.respondNoCurl()
                }
                get("/projects") {
                    if (checkCurl(call) == false) call.respondText(fetchProjects().toString(), ContentType.Text.Plain, HttpStatusCode.OK) else call.respondNoCurl()
                }
                get("/${project}/versions") {
                    if (checkCurl(call) == false) call.respondText(fetchVersions(project).toString(), ContentType.Text.Plain, HttpStatusCode.OK) else call.respondNoCurl()
                }
                get("/${project}/builds") {
                    if (checkCurl(call) == false) call.respondText("Bad request: Version parameter found\nExample: /${project}/builds/(version)\nPlease remove the surrounding parentheses when actually typing.", ContentType.Text.Plain, HttpStatusCode.BadRequest) else call.respondNoCurl()
                }
                get("/${project}/builds/{version}") {
                    val version = call.parameters["version"]!!

                    if (checkCurl(call) == false) {
                        if (fetchVersions(project).contains(version)) {
                            call.respondText(fetchBuilds(project, version).toString(), ContentType.Text.Plain, HttpStatusCode.OK)
                        } else call.respondText("Bad request: No such version found", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                    }
                    else call.respondNoCurl()
                }
                get("/${project}/download/") {
                    if (checkCurl(call) == false) call.respondText("Bad request: No version parameter found\nExample: /${project}/download/(version)/(build)\nPlease remove the surrounding parentheses when actually typing.", ContentType.Text.Plain, HttpStatusCode.BadRequest) else call.respondNoCurl()
                }
                get("${project}/download/{version}") {
                    if (checkCurl(call) == false) call.respondText("Bad request: No build parameter found\nExample: /${project}/download/(version)/(build)\nPlease remove the surrounding parentheses when actually typing.", ContentType.Text.Plain, HttpStatusCode.BadRequest) else call.respondNoCurl()
                }
                get("/${project}/download/{version}/{build}") {
                    val version = call.parameters["version"]!!
                    val build = call.parameters["build"]!!

                    if (checkCurl(call) == false) {
                        if (fetchVersions(project).contains(version)) {
                            if (fetchBuilds(project, version).contains(build)) call.respondRedirect(fetchDownload(project, version, build), true)
                            else call.respondText("Bad request: No such build found", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                        }
                        else call.respondText("Bad request: No such version found", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                    } else call.respondNoCurl()
                }
            }
        }
    }.start(wait = true)
}

suspend fun ApplicationCall.respondNoCurl() = respondText("Sorry, this API does not accept cURL requests due to our unknown IOException problems.\nPlease use other tools like wget.\nWe're very sorry for inconvenience.", ContentType.Text.Plain, HttpStatusCode.BadRequest)

fun checkCurl(call: ApplicationCall): Boolean? = call.request.headers["User-Agent"]?.contains("curl")