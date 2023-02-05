package com.assignment.ktserver.entity

import arrow.core.Either

/**
 * nested 형태로 자식 카테고리를 subCategories 형태로 가지고 있는 모델
 */
data class Category(
    val id: String,
    val parentId: String?,
    val name: String,
    val subCategories: List<Category> = emptyList(),
)

/**
 * flat 한 형태로 부모-자식간 링킹을 위해 parentId 만 가지고 있는 모델
 */
data class CategoryEntity(
    val id: String,
    val parentId: String?,
    val name: String,
) {
    companion object {
        fun validateName(name: String): Either<String, Unit> =
            if (name.filterNot { it.isDigit() || it in 'a'..'z' || it in 'A'..'Z' }.isNotEmpty())
                Either.Left("A category name should consists of alphanumeric characters.")
            else
                Either.Right(Unit)
    }
}