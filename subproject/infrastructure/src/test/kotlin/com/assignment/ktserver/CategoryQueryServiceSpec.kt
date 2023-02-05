package com.assignment.ktserver

import arrow.core.Either
import com.assignment.ktserver.entity.Category
import com.assignment.ktserver.entity.CategoryEntity
import com.assignment.ktserver.service.CategoryQueryService
import com.assignment.ktserver.util.randomAlphanumericString
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class CategoryQueryServiceSpec : FreeSpec({
    val categoryRepository = mockk<CategoryRepository>()
    val categoryQueryService = CategoryQueryServiceImpl(categoryRepository)

    fun randomCategoryEntity(): CategoryEntity =
        CategoryEntity(
            id = randomAlphanumericString(32),
            name = randomAlphanumericString(32),
            parentId = randomAlphanumericString(32)
        )

    fun randomCategory(): Category =
        Category(
            id = randomAlphanumericString(32),
            name = randomAlphanumericString(32),
            parentId = null,
            subCategories = listOf(
                Category(
                    id = randomAlphanumericString(32),
                    name = randomAlphanumericString(32),
                    parentId = randomAlphanumericString(32),
                    subCategories = emptyList()
                )
            )
        )

    afterTest {
        clearMocks(categoryRepository)
    }

    "get" - {
        val category = randomCategory()
        val categoryEntity = randomCategoryEntity()
        val updateCategory = Category(id = categoryEntity.id, parentId = categoryEntity.parentId, name = categoryEntity.name)
        "카테고리 쿼리 서비스를 조회하면 카테고리가 반환" {
            coEvery { categoryRepository.findWithChild(any()) } returns listOf(categoryEntity)
            categoryQueryService.get(categoryEntity.id) shouldBe Either.Right(updateCategory)
            coVerify(exactly = 1) { categoryRepository.findWithChild(any()) }
        }

        "카테고리 쿼리 서비스에서 찾지 못했다면 null 이 반환" {
            coEvery { categoryRepository.findWithChild(any()) } returns listOf()
            categoryQueryService.get(category.id) shouldBe Either.Right(null)
            coVerify(exactly = 1) { categoryRepository.findWithChild(any()) }
        }
    }

    "getAll" - {
        val parentEntity = randomCategoryEntity().copy(parentId = null)
        val childEntity = randomCategoryEntity().copy(parentId = parentEntity.id)

        val chileCategory = Category(id = childEntity.id, parentId = childEntity.parentId, name = childEntity.name)
        val parentCategory = Category(id = parentEntity.id, parentId = parentEntity.parentId, name = parentEntity.name, subCategories = listOf(chileCategory))
        "모든 카테고리를 조회" {
            coEvery { categoryRepository.findWithChild(null) } returns listOf(parentEntity, childEntity)
            categoryQueryService.getAll() shouldBe Either.Right(listOf(parentCategory))
            coVerify(exactly = 1) { categoryRepository.findWithChild(null) }
        }

        "카테고리가 없으면 빈 리스트가 반환." {
            coEvery { categoryRepository.findWithChild(null) } returns emptyList()
            categoryQueryService.getAll() shouldBe Either.Right(emptyList())
            coVerify(exactly = 1) { categoryRepository.findWithChild(null) }
        }
    }

    "create" - {
        val entity = randomCategoryEntity()

        "카테고리를 생성한다면 Entity 를 반환" {
            coEvery { categoryRepository.create(any()) } returns entity
            categoryQueryService.create(entity.name) shouldBe entity
            coVerify(exactly = 1) { categoryQueryService.create(any()) }
        }
    }


    "update" - {
        val category = randomCategory()
        val entity = randomCategoryEntity()
        val newCategoryName = randomAlphanumericString(32)
        val updateRow = 123

        "이름 변경 성공시 변경된 row 가 반환" {
            coEvery { categoryRepository.find(any()) } returns entity
            coEvery { categoryRepository.update(any(), any()) } returns updateRow
            categoryQueryService
                .update(category.id, name = newCategoryName) shouldBe Either.Right(Unit)
            coVerify(exactly = 1) { categoryRepository.update(category.id, newCategoryName) }
        }


        "존재하지 않는 카테고리는 업데이트 하지 않고 DoesNotExist 으로 응답" {
            coEvery { categoryRepository.find(any()) } throws Exception()
            coEvery { categoryRepository.update(any(), any()) } returns 0
            categoryQueryService
                .update(category.id, newCategoryName)
                .shouldBeLeft()
                .shouldBeTypeOf<CategoryQueryService.UpdateFailure.DoesNotExist>()
            coVerify(exactly = 1) { categoryRepository.find(any()) }
            coVerify(exactly = 0) { categoryRepository.update(any(), any()) }
        }
    }

    "delete" - {

        val parentEntity = randomCategoryEntity().copy(parentId = null)
        val childEntity = randomCategoryEntity().copy(parentId = parentEntity.id)

        val chileCategory = Category(id = childEntity.id, parentId = childEntity.parentId, name = childEntity.name)
        Category(id = parentEntity.id, parentId = parentEntity.parentId, name = parentEntity.name, subCategories = listOf(chileCategory))
        val row = 123

        "삭제에 성공하면 Unit 이 반환" {
            coEvery { categoryRepository.findWithChild(any()) } returns listOf(parentEntity, childEntity)
            coEvery { categoryRepository.deleteAll(any()) } returns row
            categoryQueryService
                .deleteAll(parentEntity.id) shouldBe Either.Right(Unit)
            coVerify(exactly = 1) { categoryRepository.deleteAll(listOf(parentEntity.id, childEntity.id)) }
        }

        "존재하지 않는 카테고리는 삭제 하지 않고 DoesNotExist 으로 응답" {
            coEvery { categoryRepository.findWithChild(any()) } returns listOf()
            coEvery { categoryRepository.deleteAll(any()) } returns null
            categoryQueryService
                .deleteAll(parentEntity.id)
                .shouldBeLeft()
                .shouldBeTypeOf<CategoryQueryService.DeleteFailure.DoesNotExist>()
            coVerify(exactly = 1) { categoryRepository.findWithChild(any()) }
            coVerify(exactly = 0) { categoryRepository.deleteAll(any()) }
        }
    }

    "connectRelationship" - {
        val category = randomCategory()
        val newParentId = randomAlphanumericString(32)
        val row = 123

        val parentEntity = randomCategoryEntity().copy(parentId = null)
        val childEntity = randomCategoryEntity().copy(parentId = parentEntity.id)

        "부모 등록 성공시 Unit 이 반환" {
            coEvery { categoryRepository.find(any()) } returns childEntity
            coEvery { categoryRepository.findWithChild(id = any()) } returns listOf(parentEntity)
            coEvery { categoryRepository.updateRelationship(parentId = any(), id = any()) } returns row
            categoryQueryService
                .connectRelationship(parentId = newParentId, id = category.id) shouldBe Either.Right(Unit)
            coVerify(exactly = 1) { categoryRepository.updateRelationship(parentId = any(), id = any()) }
        }

        "본인이나 자식 카테고리는 부모로 등록할 수 없고, NotAllowCircularReference 으로 응답" {
            coEvery { categoryRepository.find(any()) } returns childEntity
            coEvery { categoryRepository.findWithChild(any()) } returns  listOf(parentEntity, childEntity)
            coEvery { categoryRepository.updateRelationship(any(), any()) } returns row
            categoryQueryService
                .connectRelationship(childEntity.id, parentEntity.id)
                .shouldBeLeft()
                .shouldBeTypeOf<CategoryQueryService.RelationshipFailure.NotAllowCircularReference>()
            coVerify(exactly = 1) { categoryRepository.find(any()) }
            coVerify(exactly = 1) { categoryRepository.findWithChild(any()) }
            coVerify(exactly = 0) { categoryRepository.updateRelationship(any(), any()) }
        }

        "부모가 존재하지 않는 카테고리는 업데이트 하지 않고 DoesNotExist 으로 응답" {
            coEvery { categoryRepository.find(any()) } throws Exception()
            categoryQueryService
                .connectRelationship(parentId = newParentId, id = category.id) shouldBe Either.Left(
                CategoryQueryService.RelationshipFailure.DoesNotExist(category.id)
            )
            coVerify(exactly = 1) { categoryRepository.find(any()) }
            coVerify(exactly = 0) { categoryRepository.findWithChild(any()) }
            coVerify(exactly = 0) { categoryRepository.updateRelationship(parentId = any(), id = any()) }
        }

        "자식이 존재하지 않는 카테고리는 업데이트 하지 않고 DoesNotExist 으로 응답" {
            coEvery { categoryRepository.find(any()) } returns childEntity
            coEvery { categoryRepository.findWithChild(any()) } returns emptyList()
            categoryQueryService
                .connectRelationship(parentId = newParentId, id = category.id) shouldBe Either.Left(
                CategoryQueryService.RelationshipFailure.DoesNotExist(category.id)
            )
            coVerify(exactly = 1) { categoryRepository.find(any()) }
            coVerify(exactly = 1) { categoryRepository.findWithChild(any()) }
            coVerify(exactly = 0) { categoryRepository.updateRelationship(parentId = any(), id = any()) }
        }
    }
})
