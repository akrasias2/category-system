package com.assignment.ktserver.json

import kotlinx.serialization.Serializable
import com.assignment.ktserver.entity.Category as DomainCategory

@Serializable
data class Category(
    val id: String,
    val name: String,
    val subCategories: List<Category>
)

fun DomainCategory.toJson(): Category =
    Category(id = id, name = name, subCategories = subCategories.map { it.toJson() })

