package com.assignment.ktserver.service

import arrow.core.Either
import com.assignment.ktserver.entity.Category
import com.assignment.ktserver.entity.CategoryEntity

interface CategoryQueryService {
    /**
     * id 로 넘겨받은 카테고리를 하위 카테고리들까지 조회
     */
    suspend fun get(id: String): Either<GetFailure, Category?>

    /**
     * 최상위 카테고리를 조회하여 재귀적으로 하위 카테고리까지 조회
     */
    suspend fun getAll(): Either<GetFailure, List<Category>>
    suspend fun create(name: String): CategoryEntity
    suspend fun update(id: String, name: String): Either<UpdateFailure, Unit>
    suspend fun deleteAll(id: String): Either<DeleteFailure, Unit>
    suspend fun connectRelationship(parentId: String, id: String): Either<RelationshipFailure, Unit>

    data class GetFailure(val failure: String)

    sealed interface UpdateFailure {
        data class DoesNotExist(val id: String) : UpdateFailure
    }

    sealed interface DeleteFailure {
        data class DoesNotExist(val id: String) : DeleteFailure
    }

    sealed interface RelationshipFailure {
        data class DoesNotExist(val id: String) : RelationshipFailure
        data class NotAllowCircularReference(val reason: String) : RelationshipFailure
    }
}
