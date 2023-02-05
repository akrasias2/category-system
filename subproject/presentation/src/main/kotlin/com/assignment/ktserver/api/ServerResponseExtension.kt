import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import java.net.URI

// ServerResponse
suspend inline fun ok(body: Any? = null) = ServerResponse.ok().let {
    if (body != null) it.bodyValueAndAwait(body) else it.buildAndAwait()
}

suspend inline fun create(location: URI) = ServerResponse.created(location).buildAndAwait()
suspend inline fun okNoContent() = ServerResponse.noContent().buildAndAwait()

suspend inline fun notFound() = ServerResponse.notFound().buildAndAwait()
suspend inline fun badRequest(body: Any? = null) = ServerResponse.status(HttpStatus.BAD_REQUEST).let {
    if (body != null) it.bodyValueAndAwait(body) else it.buildAndAwait()
}

suspend inline fun conflict(body: Any? = null) = ServerResponse.status(HttpStatus.CONFLICT).let {
    if (body != null) it.bodyValueAndAwait(body) else it.buildAndAwait()
}

suspend inline fun internalError() = ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).buildAndAwait()
