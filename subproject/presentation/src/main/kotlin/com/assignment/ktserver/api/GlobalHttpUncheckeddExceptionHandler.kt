package com.assignment.ktserver.api

import com.assignment.ktserver.util.TxException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

@Component
@Order(-2)
class GlobalHttpUncheckeddExceptionHandler
private constructor(errorAttributes: ErrorAttributes, applicationContext: ApplicationContext) :
    AbstractErrorWebExceptionHandler(
        errorAttributes,
        WebProperties.Resources(),
        applicationContext
    ) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Autowired
    constructor(
        errorAttributes: ErrorAttributes,
        applicationContext: ApplicationContext,
        serverCodecConfigurer: ServerCodecConfigurer
    ) : this(errorAttributes, applicationContext) {
        super.setMessageReaders(serverCodecConfigurer.readers)
        super.setMessageWriters(serverCodecConfigurer.writers)
    }

    override fun getRoutingFunction(
        errorAttributes: ErrorAttributes?
    ): RouterFunction<ServerResponse> {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse)
    }

    private fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> {
        val error = getError(request)
        log.error(error.stackTraceToString())
        val msg = when(error) {
            is TxException -> "Failed Transaction Error"
            else -> "Internal Error"
        }
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(msg)
    }

}
