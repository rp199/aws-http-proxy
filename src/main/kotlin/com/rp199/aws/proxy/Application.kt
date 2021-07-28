package com.rp199.aws.proxy

import com.rp199.aws.proxy.auth.AwsHttpRequestHeaderSigner
import com.rp199.aws.proxy.plugins.configureMonitoring
import com.rp199.aws.proxy.plugins.configureRouting
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.logging.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider
import software.amazon.awssdk.auth.signer.AsyncAws4Signer

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureMonitoring()
        kodein {
            bind<HttpClient>() with singleton {
                HttpClient(CIO) {
                    install(Logging)
                    expectSuccess = false
                }
            }
            bind<AsyncAws4Signer>() with singleton { AsyncAws4Signer.create() }
            bind<AwsCredentialsProvider>() with singleton {
                AwsCredentialsProviderChain.of(
                    //EnvironmentVariableCredentialsProvider.create(),
                    SystemPropertyCredentialsProvider.create(),
                    ProfileCredentialsProvider.create()
                )
            }
            bind<AwsHttpRequestHeaderSigner>() with singleton { AwsHttpRequestHeaderSigner(instance(), instance()) }
        }
    }.start(wait = true)
}
