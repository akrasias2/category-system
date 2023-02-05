package com.assignment.ktserver

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.continuations.either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.assignment.ktserver.CategoryQueryServiceImpl.Dto.MutableCategory.Companion.toDomain
import com.assignment.ktserver.entity.Category
import com.assignment.ktserver.entity.CategoryEntity
import com.assignment.ktserver.service.CategoryQueryService
import com.assignment.ktserver.service.CategoryQueryService.*
import com.assignment.ktserver.util.TxException
import com.assignment.ktserver.util.leftOrUnit
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryQueryServiceImpl(private val repository: CategoryRepository) : CategoryQueryService {

    override suspend fun get(id: String): Either<GetFailure, Category?> = catch {
        repository.findWithChild(id = id)
            .toDomain(id)
            .firstOrNull()
    }.mapLeft { GetFailure(it.stackTraceToString()) }

    override suspend fun getAll(): Either<GetFailure, List<Category>> = catch {
        repository.findWithChild(null).toDomain()
    }.mapLeft { GetFailure(it.stackTraceToString()) }

    @Transactional
    override suspend fun create(name: String): CategoryEntity = repository.create(name = name)

    @Transactional
    override suspend fun update(id: String, name: String): Either<UpdateFailure, Unit> = either {
        catch { repository.find(id) }
            .mapLeft { UpdateFailure.DoesNotExist(id) }
            .bind()

        catch {
            repository.update(id = id, name = name)
        }.mapLeft { throw TxException() }
            .bind()
    }

    @Transactional
    override suspend fun deleteAll(id: String): Either<DeleteFailure, Unit> = either {
        val existCategories = catch { repository.findWithChild(id) }
            .mapLeft { DeleteFailure.DoesNotExist(id) }
            .flatMap { categories ->
                if (categories.isEmpty()) DeleteFailure.DoesNotExist(id).left()
                else categories.right()
            }.bind()

        val deleteIds = existCategories.map { it.id }
        catch { repository.deleteAll(deleteIds) }
            .mapLeft { throw TxException() }
            .bind()
    }

    @Transactional
    override suspend fun connectRelationship(parentId: String, id: String): Either<RelationshipFailure, Unit> = either {
        // 부모 존재 확인 절차
        catch { repository.find(parentId) }
            .mapLeft { RelationshipFailure.DoesNotExist(id) }
            .bind()

        // 본인 및 자식 존재 확인 절차
        val categories =
            catch { repository.findWithChild(id) }
                .mapLeft { RelationshipFailure.DoesNotExist(id) }
                .flatMap { categories ->
                    if (categories.isEmpty()) RelationshipFailure.DoesNotExist(id).left()
                    else categories.right()
                }.bind()

        // 부모-자식 간 순환참조 방지 과정
        if (categories.find { it.id == parentId } != null) {
            val msg = "Cannot register your own category or child category as a parent category."
            RelationshipFailure.NotAllowCircularReference(msg).leftOrUnit().bind()
        }

        // 부모-자식 관계 업데이트
        catch { repository.updateRelationship(parentId, id) }
            .mapLeft { throw TxException() }
            .flatMap {
                if (it == null || it == 0) RelationshipFailure.DoesNotExist(id).left()
                else it.right()
            }
            .bind()
    }

    companion object Dto {

        // flat categories -> nested categories for parent-child relationship
        private fun List<CategoryEntity>.toDomain(id: String? = null): List<Category> {
            val mutableCategories = map { MutableCategory(it.id, it.parentId, it.name) }
            val parentCategoryGroup = mutableCategories.groupBy { it.parentId }
            val resultMap = parentCategoryGroup.mapValues { mapEntry ->
                mapEntry.value.map { parent ->
                    parent.apply { subCategories = parentCategoryGroup[parent.id].orEmpty() }
                }
            }
            val rootId = find { it.id == id }?.parentId
            return resultMap[rootId].orEmpty().map { it.toDomain() }
        }

        private class MutableCategory(
            val id: String,
            val parentId: String?,
            val name: String,
            var subCategories: List<MutableCategory> = emptyList(),
        ) {
            companion object {
                fun MutableCategory.toDomain(): Category =
                    Category(id, parentId, name, subCategories = subCategories.map { it.toDomain() })
            }
        }
    }
}