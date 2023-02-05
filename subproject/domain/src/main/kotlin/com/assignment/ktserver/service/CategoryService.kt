package com.assignment.ktserver.service

import arrow.core.Either
import arrow.core.continuations.either
import com.assignment.ktserver.entity.Category
import com.assignment.ktserver.entity.CategoryEntity
import com.assignment.ktserver.entity.CategoryEntity.Companion.validateName
import org.springframework.stereotype.Service

@Service
class CategoryService(private val service: CategoryQueryService) {

    suspend fun get(id: String): Either<GetFailure, Category?> = either {
        service.get(id = id).bind()
    }.mapLeft { GetFailure(it.failure) }

    suspend fun getAll(): Either<GetFailure, List<Category>?> = either {
        service.getAll().bind()
    }.mapLeft { GetFailure(it.failure) }

    suspend fun create(name: String): Either<CreateFailure, CategoryEntity> = either {
        validateName(name).mapLeft { CreateFailure.InvalidName(name, it) }
            .bind()
        service.create(name = name)
    }

    suspend fun update(id: String, name: String): Either<UpdateFailure, Unit> = either {
        validateName(name)
            .mapLeft { UpdateFailure.InvalidName(name, it) }
            .bind()

        service.update(id = id, name = name).mapLeft {
            when (it) {
                is CategoryQueryService.UpdateFailure.DoesNotExist
                -> UpdateFailure.DoesNotExist(it.id)
            }
        }.bind()
    }

    suspend fun deleteAll(id: String): Either<DeleteFailure, Unit> = either {
        service.deleteAll(id = id).mapLeft {
            when (it) {
                is CategoryQueryService.DeleteFailure.DoesNotExist
                -> DeleteFailure.DoesNotExist(it.id)
            }
        }.bind()
    }

    suspend fun connectRelationship(parentId: String, id: String): Either<RelationshipFailure, Unit> = either {
        service.connectRelationship(parentId = parentId, id = id).mapLeft {
            when (it) {
                is CategoryQueryService.RelationshipFailure.DoesNotExist
                -> RelationshipFailure.DoesNotExist(it.id)

                is CategoryQueryService.RelationshipFailure.NotAllowCircularReference
                -> RelationshipFailure.NotAllowCircularReference(it.reason)
            }
        }.bind()
    }

    data class GetFailure(val failure: String)

    sealed interface CreateFailure {
        data class InvalidName(val name: String, val reason: String) : CreateFailure
    }

    sealed interface UpdateFailure {
        data class DoesNotExist(val id: String) : UpdateFailure
        data class InvalidName(val name: String, val reason: String) : UpdateFailure
    }

    sealed interface DeleteFailure {
        data class DoesNotExist(val id: String) : DeleteFailure
    }

    sealed interface RelationshipFailure {
        data class DoesNotExist(val id: String) : RelationshipFailure
        data class NotAllowCircularReference(val reason: String) : RelationshipFailure
    }
}
