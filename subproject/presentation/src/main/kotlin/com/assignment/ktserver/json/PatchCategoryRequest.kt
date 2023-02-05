package com.assignment.ktserver.json

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class PatchCategoryRequest(
    @Required
    val name: String
)
