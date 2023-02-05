package com.assignment.ktserver.json

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostCategoryRelationshipRequest(
    @Required
    @SerialName("parent_id")
    val parentId: String,
    @Required
    val id: String,
)