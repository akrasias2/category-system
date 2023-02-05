package com.assignment.ktserver

import com.assignment.ktserver.entity.CategoryEntity

interface CategoryRepository {
    /**
     * 단건만 조회
     */
    suspend fun find(id: String): CategoryEntity?
    /**
     * 연결된 자식 카테고리까지 조회
     */
    suspend fun findWithChild(id: String?): List<CategoryEntity>
    suspend fun create(name: String): CategoryEntity
    suspend fun update(id: String, name: String) : Int?
    suspend fun deleteAll(ids: List<String>) : Int?
    suspend fun updateRelationship(parentId : String, id : String) : Int?
}
