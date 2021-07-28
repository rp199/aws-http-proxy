package com.rp199.aws.proxy.plugins

import com.rp199.aws.proxy.auth.AwsHttpRequestHeaderSigner
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import java.net.URI

const val defaultPath = "https://some-endpoint.com"
val unsafeHeaders = listOf("Content-Type", "Content-Length").map { it.lowercase() }

fun Application.configureRouting() {
    install(Locations) {
    }
    routing {
        route("{...}") {
            val client by kodein().instance<HttpClient>()
            val signer by kodein().instance<AwsHttpRequestHeaderSigner>()

            HttpMethod.DefaultMethods.forEach { inboundRequestMethod ->
                method(inboundRequestMethod) {
                    handle {
                        val path: String = call.request.path()
                        val inboundHeaders = call.request.headers
                        val queryString = call.request.queryString().takeIf { it.isNotEmpty() }?.let { "?$it" } ?: ""
                        val targetPath = "$defaultPath$path$queryString"
                        val inboundBody = call.receiveText().takeUnless { it.isBlank() }

                        val signedHeaders = signer.sign(inboundRequestMethod, URI.create(targetPath), inboundBody)
                        val response: HttpResponse = client.request(targetPath) {
                            method = inboundRequestMethod
                            headers {
                                appendAll(inboundHeaders.filter { k, _ -> k !in signedHeaders })
                                appendAll(signedHeaders)
                            }
                            inboundBody?.let {
                                body = it
                            }
                        }

                        response.headers.filter { s, _ -> s.lowercase() !in unsafeHeaders }.forEach { s, list -> call.response.headers.append(s, list.first()) }
                        call.respondText(contentType = response.contentType(), status = response.status) {
                            response.receive()
                        }
                    }
                }
            }
        }
    }
}
