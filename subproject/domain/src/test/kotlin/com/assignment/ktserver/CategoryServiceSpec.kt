package com.assignment.ktserver

import arrow.core.Either
import com.assignment.ktserver.entity.Category
import com.assignment.ktserver.entity.CategoryEntity
import com.assignment.ktserver.service.CategoryQueryService
import com.assignment.ktserver.service.CategoryService
import com.assignment.ktserver.util.randomAlphanumericString
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class CategoryServiceSpec : FreeSpec({
    val categoryQueryService = mockk<CategoryQueryService>()
    val categoryService = CategoryService(categoryQueryService)

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
        clearMocks(categoryQueryService)
    }

    "get" - {
        val category = randomCategory()
        "카테고리 조회 성공시 카테고리 반환" {
            coEvery { categoryQueryService.get(category.id) } returns Either.Right(category)
            categoryService.get(category.id) shouldBe Either.Right(category)
            coVerify(exactly = 1) { categoryQueryService.get(category.id) }
        }

        "카테고리를 찾지 못했다면 null 반환. " {
            coEvery { categoryQueryService.get(category.id) } returns Either.Right(null)
            categoryService.get(category.id) shouldBe Either.Right(null)
            coVerify(exactly = 1) { categoryQueryService.get(category.id) }
        }
    }

    "getAll" - {
        val category = randomCategory()
        "모든 카테고리를 조회" {
            coEvery { categoryQueryService.getAll() } returns Either.Right(listOf(category))
            categoryService.getAll() shouldBe Either.Right(listOf(category))
            coVerify(exactly = 1) { categoryQueryService.getAll() }
        }

        "카테고리가 없으면 빈 리스트 반환." {
            coEvery { categoryQueryService.getAll() } returns Either.Right(emptyList())
            categoryService.getAll() shouldBe Either.Right(emptyList())
            coVerify(exactly = 1) { categoryQueryService.getAll() }
        }
    }

    "create" - {
        val entity = randomCategoryEntity()

        "카테고리를 생성한다면 Entity 반환" {
            coEvery { categoryQueryService.create(entity.name) } returns entity
            categoryService.create(entity.name) shouldBe Either.Right(entity)
            coVerify(exactly = 1) { categoryService.create(entity.name) }
        }

        "이름이 유효하지 않으면 InvalidName 에러가 발생하고, create 가 호출되지 않음" {
            categoryService
                .create("!@#$%")
                .shouldBeLeft()
                .shouldBeTypeOf<CategoryService.CreateFailure.InvalidName>()
            coVerify(exactly = 0) { categoryQueryService.create(any()) }
        }
    }


    "update" - {
        val category = randomCategory()
        val newCategoryName = randomAlphanumericString(32)

        "이름 변경 성공시 Unit 이 반환" {
            coEvery { categoryQueryService.update(category.id, newCategoryName) } returns Either.Right(Unit)
            categoryService
                .update(category.id, name = newCategoryName) shouldBe Either.Right(Unit)
            coVerify(exactly = 1) { categoryQueryService.update(category.id, newCategoryName) }
        }

        "이름이 유효하지 않으면 InvalidName 에러가 발생하고, updete 가 호출되지 않음" {
            coEvery { categoryQueryService.update(category.id, "!@#$%") } returns Either.Right(Unit)
            categoryService
                .update(category.id, "!@#$%")
                .shouldBeLeft()
                .shouldBeTypeOf<CategoryService.UpdateFailure.InvalidName>()
            coVerify(exactly = 0) { categoryQueryService.update(category.id, "!@#$%") }
        }

        "존재하지 않는 카테고리는 업데이트 하지 않음 DoesNotExist 으로 응답" {
            coEvery { categoryQueryService.update(category.id, newCategoryName) } returns Either.Left(
                CategoryQueryService.UpdateFailure.DoesNotExist(category.id)
            )
            categoryService
                .update(category.id, newCategoryName) shouldBe Either.Left(
                CategoryService.UpdateFailure.DoesNotExist(category.id)
            )
            coVerify(exactly = 1) { categoryQueryService.update(category.id, newCategoryName) }
        }
    }

    "delete" - {
        val category = randomCategory()

        "삭제에 성공하면 Unit 이 반환" {
            coEvery { categoryQueryService.deleteAll(category.id) } returns Either.Right(Unit)
            categoryService
                .deleteAll(category.id) shouldBe Either.Right(Unit)
            coVerify(exactly = 1) { categoryQueryService.deleteAll(category.id) }
        }

        "존재하지 않는 카테고리는 삭제 하지 않고 DoesNotExist 으로 응답" {
            coEvery { categoryQueryService.deleteAll(category.id) } returns Either.Left(
                CategoryQueryService.DeleteFailure.DoesNotExist(category.id)
            )
            categoryService
                .deleteAll(category.id) shouldBe Either.Left(CategoryService.DeleteFailure.DoesNotExist(category.id))
            coVerify(exactly = 1) { categoryQueryService.deleteAll(category.id) }
        }
    }

    "connectRelationship" - {
        val category = randomCategory()
        val newParentId = randomAlphanumericString(32)

        "부모 등록 성공시 Unit 이 반환" {
            coEvery { categoryQueryService.connectRelationship(parentId = newParentId, id = category.id) } returns Either.Right(Unit)
            categoryService
                .connectRelationship(parentId = newParentId, id = category.id) shouldBe Either.Right(Unit)
            coVerify(exactly = 1) { categoryQueryService.connectRelationship(parentId = newParentId, id = category.id) }
        }

        "본인이나 자식 카테고리는 부모로 등록할 수 없고, NotAllowCircularReference 으로 응답" {
            coEvery { categoryQueryService.connectRelationship(newParentId, category.id) } returns Either.Left(
                CategoryQueryService.RelationshipFailure.NotAllowCircularReference("")
            )
            categoryService
                .connectRelationship(newParentId, category.id)
                .shouldBeLeft()
                .shouldBeTypeOf<CategoryService.RelationshipFailure.NotAllowCircularReference>()
            coVerify(exactly = 1) { categoryQueryService.connectRelationship(newParentId, category.id) }
        }

        "존재하지 않는 카테고리는 업데이트 하지 않고 DoesNotExist 으로 응답" {
            coEvery { categoryQueryService.connectRelationship(parentId = newParentId, id = category.id) } returns Either.Left(
                CategoryQueryService.RelationshipFailure.DoesNotExist(category.id)
            )
            categoryService
                .connectRelationship(parentId = newParentId, id = category.id) shouldBe Either.Left(
                CategoryService.RelationshipFailure.DoesNotExist(category.id)
            )
            coVerify(exactly = 1) { categoryQueryService.connectRelationship(parentId = newParentId, id = category.id) }
        }
    }
})
