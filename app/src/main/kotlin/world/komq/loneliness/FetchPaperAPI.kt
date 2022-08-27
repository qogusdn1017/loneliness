package world.komq.loneliness

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

object FetchPaperAPI {
    private const val paperAPI = "https://papermc.io/api/v2"

    private fun request(url: String): String {
        val client = HttpClient.newHttpClient()
        val builder = HttpRequest.newBuilder(URI.create(url))

        builder.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11")
        builder.header("Cache-Control", "no-cache, no-store, must-revalidate")
        builder.header("Pragma", "no-cache")
        builder.header("Expires", "0")

        builder.method("GET", HttpRequest.BodyPublishers.ofString(""))

        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body().toString()
    }

    fun fetchProjects(): List<String> = Json.parseToJsonElement(request("${paperAPI}/projects")).jsonObject["projects"]!!.jsonArray.map { it.toString() }.toMutableList().apply { remove("\"travertine\"") }

    fun fetchVersions(project: String): List<String> = Json.parseToJsonElement(request("${paperAPI}/projects/${project}")).jsonObject["versions"]!!.jsonArray.map { it.toString().replace("\"", "") }

    fun fetchBuilds(project: String, version: String): List<String> = Json.parseToJsonElement(request("${paperAPI}/projects/${project}/versions/${version}")).jsonObject["builds"]!!.jsonArray.map { it.toString().replace("\"", "") }

    fun fetchDownload(project: String, version: String, build: String): String = "${paperAPI}/projects/${project}/versions/${version}/builds/${build}/downloads/${project}-${version}-${build}.jar"

    fun fetchLatestArtifact(project: String): String {
        val latestVersion = fetchVersions(project).last()
        val latestBuild = fetchBuilds(project, latestVersion).last()
        return fetchDownload(project, latestVersion, latestBuild)
    }
}