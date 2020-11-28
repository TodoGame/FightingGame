package com.somegame.responseExceptions

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.*

class TransformExceptionsIntoResponses {

    class Configuration

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, TransformExceptionsIntoResponses> {
        override val key = AttributeKey<TransformExceptionsIntoResponses>("TransfortExceptionsIntoResponses")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): TransformExceptionsIntoResponses {
            val feature = TransformExceptionsIntoResponses()

            pipeline.intercept(ApplicationCallPipeline.Features) {
                try {
                    this.proceed()
                } catch (e: ExceptionWithResponse) {
                    call.respondText(e.message ?: "", ContentType.Text.Plain, e.statusCode)
                }
            }
            return feature
        }
    }
}
