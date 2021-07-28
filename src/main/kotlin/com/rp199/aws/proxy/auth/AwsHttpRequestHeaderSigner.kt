package com.rp199.aws.proxy.auth

import io.ktor.http.*
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.signer.AsyncAws4Signer
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.SdkHttpFullRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.regions.Region
import java.net.URI

class AwsHttpRequestHeaderSigner(private val aws4Signer: AsyncAws4Signer, private val awsCredentialsProvider: AwsCredentialsProvider) {

    private val awsSigV4Params = Aws4SignerParams.builder()
        .awsCredentials(awsCredentialsProvider.resolveCredentials())
        //TODO move this to properties
        .signingRegion(Region.EU_WEST_1)
        .signingName("execute-api")
        .build()

    fun sign(method: HttpMethod, uri: URI, body: String? = null) = withSdkRequest(method, uri, body) {
        val signedHeaders = aws4Signer.sign(it, awsSigV4Params).headers()
        Headers.build {
            signedHeaders.forEach { (k, v) -> appendAll(k, v) }
        }

    }

    private fun withSdkRequest(method: HttpMethod, uri: URI, body: String? = null, block: (SdkHttpFullRequest) -> Headers) = SdkHttpFullRequest.builder()
        .method(SdkHttpMethod.fromValue(method.value))
        .uri(uri)
        .apply { body?.let { contentStreamProvider(RequestBody.fromString(it).contentStreamProvider()) } }
        .build().let(block)

}