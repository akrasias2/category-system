package com.assignment.ktserver

import com.assignment.ktserver.configuration.DataSourceProperty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.await

@SpringBootTest(classes = [TestApplication::class])
class CategoryRepositorySpec(
    @Autowired val repository: CategoryRepository,
    @Autowired val dataSourceProperty: DataSourceProperty
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val template = R2dbcEntityTemplate(dataSourceProperty.connectionFactory())
    private val databaseClient = template.databaseClient

    @BeforeEach
    fun setup() = runBlocking {
        val sql = """TRUNCATE table category;""".trimIndent()
        databaseClient.sql(sql).await()
    }

    @Test
    fun `카테고리 생성 및 단건 찾기`() = runBlocking {
        // given
        val givenCategory1 = repository.create("TestCategory1")
        val givenCategory2 = repository.create("TestCategory2")
        // when & then
        repository.find(givenCategory1.id) shouldBe givenCategory1
        repository.find(givenCategory2.id) shouldBe givenCategory2
    }

    @Test
    fun `카테고리 부모 등록`() = runBlocking {
        // given
        val givenChild = repository.create("TestCategory1")
        val givenParent = repository.create("TestCategory2")

        repository.updateRelationship(parentId = givenParent.id, id = givenChild.id)

        // when
        val result = repository.findWithChild(givenParent.id)
        // then
        result.size shouldBe 2
        result.find { it.parentId == null }?.id shouldBe givenParent.id
        result.find { it.parentId != null }?.id shouldBe givenChild.id
        result.find { it.parentId != null }?.parentId shouldBe givenParent.id
    }

    @Test
    fun `카테고리 부모-자식 관계 list 찾기`() = runBlocking {
        // given
        val givenEntityCount = 100
        val givenFindCount = 50
        val givenSaveEntities = (0 until givenEntityCount).map {
            repository.create("TestCategory$it")
        }
        // 부모 등록
        givenSaveEntities.forEachIndexed { index, categoryEntity ->
            if (index > 0) {
                repository.updateRelationship(parentId = givenSaveEntities[index - 1].id, id = categoryEntity.id)
            }
        }

        // when
        // findCount 순번의 ID로 조회
        val findEntities = repository.findWithChild(givenSaveEntities[givenFindCount].id).also {
            log.info(it.toString())
        }

        // then
        // 조회한 ID 이후의 데이터들만 있는지 검증
        findEntities.size shouldBe givenFindCount
        findEntities.map { it.id } shouldBe givenSaveEntities.slice(givenFindCount until givenEntityCount).map { it.id }
    }

    @Test
    fun `카테고리 전체 list 찾기`() = runBlocking {
        // given
        val givenEntityCount = 3
        val givenSaveEntities = (0 until givenEntityCount).map {
            repository.create("TestCategory$it")
        }
        givenSaveEntities.forEachIndexed { index, categoryEntity ->
            if (index > 0) {
                repository.updateRelationship(parentId = givenSaveEntities[index - 1].id, id = categoryEntity.id)
            }
        }
        val givenAddEntity = listOf("TestCategoryAdd1", "TestCategoryAdd2").map {
            repository.create(it)
        }

        // when
        // id 를 포함하지 않고 조회
        val findEntities = repository.findWithChild(id = null).also {
            log.info(it.toString())
        }

        // then
        // 추가건 까지 모두 조회가 되는지 검증
        findEntities.size shouldBe givenEntityCount + givenAddEntity.size
        findEntities.map { it.id }.containsAll(givenSaveEntities.map { it.id } + givenAddEntity.map { it.id }) shouldBe true
    }

    @Test
    fun `카테고리 메타 정보 업데이트`() = runBlocking {
        // given
        val category1 = repository.create("TestCategory1")
        val category2 = repository.create("TestCategory2")

        val givenUpdateName1 = "UpdateCategory1"
        val givenUpdateName2 = "UpdateCategory2"
        repository.update(category1.id, givenUpdateName1)
        repository.update(category2.id, givenUpdateName2)

        // when & then
        repository.find(category1.id)?.name shouldBe givenUpdateName1
        repository.find(category2.id)?.name shouldBe givenUpdateName2
    }

    @Test
    fun `카테고리 리소스 삭제`() = runBlocking {
        // given
        val givenEntityCount = 100
        val givenFindCount = 50
        val givenSaveEntities = (0 until givenEntityCount).map {
            repository.create("TestCategory$it")
        }
        givenSaveEntities.forEachIndexed { index, categoryEntity ->
            if (index > 0) {
                repository.updateRelationship(parentId = givenSaveEntities[index - 1].id, id = categoryEntity.id)
            }
        }

        // when
        val findEntities = repository.findWithChild(givenSaveEntities[givenFindCount].id).also {
            log.info(it.toString())
        }

        repository.deleteAll(findEntities.map { it.id }).also {
            log.info(it.toString())
        }

        val findEntitiesAfterDelete = repository.findWithChild(givenSaveEntities[0].id).also {
            log.info(it.toString())
        }

        // then
        // 삭제 id 이전 데이터만 조회가 되는지 검증
        findEntitiesAfterDelete.size shouldBe givenFindCount
        findEntitiesAfterDelete.map { it.id } shouldBe givenSaveEntities.slice(0 until givenFindCount).map { it.id }
    }

}
