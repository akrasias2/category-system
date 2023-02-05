package com.assignment.ktserver.api

import badRequest
import com.assignment.ktserver.json.PatchCategoryRequest
import com.assignment.ktserver.json.PostCategoryRelationshipRequest
import com.assignment.ktserver.json.PostCategoryRequest
import com.assignment.ktserver.json.toJson
import com.assignment.ktserver.service.CategoryService
import com.assignment.ktserver.service.CategoryService.*
import conflict
import create
import internalError
import notFound
import ok
import okNoContent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import java.net.URI

@Controller
class CategoryApi(private val categoryService: CategoryService) {
    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun getCategory(serverRequest: ServerRequest): ServerResponse {
        val id = serverRequest.pathVariable("id")
        return categoryService.get(id).fold(
            ifRight = { category ->
                if (category != null) {
                    ok(category.toJson())
                } else {
                    notFound()
                }
            },
            ifLeft = {
                log.error(it.failure)
                internalError()
            }
        )
    }

    suspend fun getCategories(serverRequest: ServerRequest): ServerResponse {
        return categoryService.getAll().fold(
            ifRight = { categories ->
                if (!categories.isNullOrEmpty()) {
                    ok(categories.map { it.toJson() })
                } else {
                    notFound()
                }
            },
            ifLeft = {
                log.error(it.failure)
                internalError()
            }
        )
    }

    suspend fun postCategories(serverRequest: ServerRequest): ServerResponse {
        val request = runCatching {
            serverRequest.awaitBody<PostCategoryRequest>()
        }.getOrElse {
            return badRequest("Please your request information")
        }
        return categoryService.create(name = request.name).fold(
            ifRight = { create(URI.create("/categories/${it.id}")) },
            ifLeft = {
                when (it) {
                    is CreateFailure.InvalidName -> badRequest("Category name must be alphanumeric.")
                }
            }
        )
    }

    suspend fun patchCategory(serverRequest: ServerRequest): ServerResponse {
        val id = serverRequest.pathVariable("id")
        val request = runCatching {
            serverRequest.awaitBody<PatchCategoryRequest>()
        }.getOrElse {
            return badRequest("Please your request information")
        }
        return categoryService.update(id = id, name = request.name).fold(
            ifRight = { okNoContent() },
            ifLeft = {
                when (it) {
                    is UpdateFailure.DoesNotExist -> notFound()
                    is UpdateFailure.InvalidName -> badRequest("User name must be alphanumeric.")
                }
            }
        )
    }

    suspend fun deleteCategory(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id")
        return categoryService.deleteAll(id).fold(
            ifRight = { okNoContent() },
            ifLeft = {
                when (it) {
                    is DeleteFailure.DoesNotExist -> notFound()
                }
            }
        )
    }

    suspend fun postRelationship(serverRequest: ServerRequest): ServerResponse {
        val request = runCatching {
            serverRequest.awaitBody<PostCategoryRelationshipRequest>()
        }.getOrElse {
            return badRequest("Please your request information")
        }
        return categoryService.connectRelationship(parentId = request.parentId, request.id).fold(
            ifRight = { okNoContent() },
            ifLeft = {
                when (it) {
                    is RelationshipFailure.NotAllowCircularReference -> conflict(it.reason)
                    is RelationshipFailure.DoesNotExist -> notFound()
                }
            }
        )
    }

}