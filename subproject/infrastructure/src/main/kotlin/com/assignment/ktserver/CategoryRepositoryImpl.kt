package com.assignment.ktserver

import com.assignment.ktserver.configuration.DataSourceProperty
import com.assignment.ktserver.entity.CategoryEntity
import com.assignment.ktserver.util.randomAlphanumericString
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.*
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional


@Repository
class CategoryRepositoryImpl(dataSourceProperty: DataSourceProperty) : CategoryRepository {
    private val template = R2dbcEntityTemplate(dataSourceProperty.connectionFactory())
    private val databaseClient = template.databaseClient
    private val converter = template.converter

    override suspend fun find(id: String): CategoryEntity = template
        .selectOne(query(where("id").`is`(id)), CategoryRow::class.java)
        .awaitSingle()
        .toDomain()

    @Transactional
    override suspend fun create(name: String) = template
        .insert(
            CategoryRow(
                id = randomAlphanumericString(32),
                parentId = null,
                name = name
            )
        ).awaitSingle()
        .toDomain()

    @Transactional(isolation = Isolation.READ_COMMITTED)
    override suspend fun update(id: String, name: String): Int? = template
        .update<CategoryRow>()
        .inTable(CATEGORY_TABLE)
        .matching(
            query(where(CATEGORY_ID).`is`(id))
        )
        .apply(
            Update.update(CATEGORY_NAME, name)
        )
        .awaitSingleOrNull()

    @Transactional
    override suspend fun deleteAll(ids: List<String>): Int? = template
        .delete<CategoryRow>()
        .matching(query(where(CATEGORY_ID).`in`(ids)))
        .all()
        .awaitSingle()

    @Transactional
    override suspend fun updateRelationship(parentId: String, id: String): Int? = template
        .update<CategoryRow>()
        .inTable(CATEGORY_TABLE)
        .matching(
            query(where(CATEGORY_ID).`is`(id))
        )
        .apply(
            Update.update(CATEGORY_PARENT_CHILD_ID, parentId)
        ).awaitSingleOrNull()

    override suspend fun findWithChild(id: String?): List<CategoryEntity> {
        // id 검색 또는 id == null 일 때는 최상위 카테고리부터 검색
        val whereCondition = if (!id.isNullOrEmpty()) "parent.id = '$id'" else "parent_id is null"
        val sql = """
                WITH RECURSIVE recursive_cte (id, parent_id, name) AS (
                    SELECT parent.id, parent.parent_id, parent.name
                    FROM $CATEGORY_TABLE parent WHERE $whereCondition
                    UNION ALL
                    SELECT child.id, child.parent_id, child.name
                    FROM $CATEGORY_TABLE child
                    JOIN recursive_cte ON child.parent_id = recursive_cte.id )
                SELECT * FROM recursive_cte;
                """.trimIndent()

        return databaseClient
            .sql(sql)
            .convert<CategoryRow>(converter)
            .all()
            .collectList()
            .awaitSingle()
            .map { it.toDomain() }
    }

    companion object {
        const val CATEGORY_TABLE = "category"
        const val CATEGORY_ID = "id"
        const val CATEGORY_PARENT_CHILD_ID = "parent_id"
        const val CATEGORY_NAME = "name"
    }

    @Table(CATEGORY_TABLE)
    private data class CategoryRow(
        @Column(CATEGORY_ID) val id: String,
        @Column(CATEGORY_PARENT_CHILD_ID) val parentId: String?,
        @Column(CATEGORY_NAME) val name: String,
    )

    private fun CategoryRow.toDomain(): CategoryEntity = CategoryEntity(id, parentId, name)
}
