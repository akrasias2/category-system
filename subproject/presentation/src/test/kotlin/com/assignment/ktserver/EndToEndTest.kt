package com.assignment.ktserver

import com.assignment.ktserver.configuration.DataSourceProperty
import com.assignment.ktserver.json.Category
import com.assignment.ktserver.json.PatchCategoryRequest
import com.assignment.ktserver.json.PostCategoryRelationshipRequest
import com.assignment.ktserver.json.PostCategoryRequest
import com.assignment.ktserver.util.randomAlphanumericString
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.http.HttpStatus
import org.springframework.r2dbc.core.await
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.time.Duration


@SpringBootTest(classes = [TestApplication::class])
class EndToEndTest(
    @Autowired private val routerConfiguration: ApiRouter,
    @Autowired val dataSourceProperty: DataSourceProperty
) {
    @Autowired
    lateinit var repository: CategoryRepository

    private val template = R2dbcEntityTemplate(dataSourceProperty.connectionFactory())
    private val databaseClient = template.databaseClient
    private val webTestClient = WebTestClient.bindToRouterFunction(routerConfiguration.requestRouter())
        .configureClient()
        .responseTimeout(Duration.ofSeconds(20)).build()

    @BeforeEach
    fun setup() = runBlocking {
        val sql = """TRUNCATE table category;""".trimIndent()
        databaseClient.sql(sql).await()
    }

    @Test
    fun `카테고리 201 생성`() {
        // given
        val request = PostCategoryRequest(name = "HelloCategory")

        // when
        webTestClient.post()
            .uri("/categories")
            .bodyValue(request)
            .exchange()
            .expectBody()
            .consumeWith {
                // then
                it.status shouldBe HttpStatus.CREATED
            }
    }

    @Test
    fun `카테고리 생성 400 유효하지 않은 이름`() {
        // given
        val request = PostCategoryRequest(name = "Hel^&^/''loCategory")

        // when
        webTestClient.post()
            .uri("/categories")
            .bodyValue(request)
            .exchange()
            .expectBody()
            .consumeWith {
                // then
                it.status shouldBe HttpStatus.BAD_REQUEST
            }
    }

    @Test
    fun `카테고리 메타정보 업데이트 204 성공`() {
        // given
        val updateName = "UpdateCategoryName1"
        val givenCategory1 = runBlocking { repository.create("TestCategoryName1") }
        val request = PatchCategoryRequest(name = updateName)
        
        // when
        webTestClient.patch()
            .uri("/categories/${givenCategory1.id}")
            .bodyValue(request)
            .exchange()
            .expectBody()
            .consumeWith {
                // then
                it.status shouldBe HttpStatus.NO_CONTENT
            }
    }

    @Test
    fun `카테고리 단건 조회 200 OK`() {
        // given
        val givenEntityCount = 3
        val givenSaveEntities = (0 until givenEntityCount).map {
            runBlocking { repository.create("TestCategory$it")}
        }
        givenSaveEntities.forEachIndexed { index, categoryEntity ->
            if (index > 0) {
                runBlocking { repository.updateRelationship(parentId = givenSaveEntities[index - 1].id, id = categoryEntity.id)}
            }
        }

        // when
        webTestClient.get()
            .uri("/categories/${givenSaveEntities[0].id}")
            .exchange()
            .expectBody<Category>()
            .consumeWith {
                // then
                val result = it.responseBody
                it.status shouldBe HttpStatus.OK
                result?.id shouldBe givenSaveEntities[0].id
                result?.subCategories?.get(0)?.id shouldBe givenSaveEntities[1].id
            }
    }

    @Test
    fun `카테고리 전체 조회 404 NotFound`() {
        // when
        webTestClient.get()
            .uri("/categories")
            .exchange()
            .expectBody<List<Category>>()
            .consumeWith {
                // then
                it.status shouldBe HttpStatus.NOT_FOUND
            }
    }

    @Test
    fun `카테고리 전체 조회 200 OK`() {
        // given
        val givenCategory1 = runBlocking { repository.create("TestCategoryName1") }
        val givenCategory2 = runBlocking { repository.create("TestCategoryName2") }

        // when
        webTestClient.get()
            .uri("/categories")
            .exchange()
            .expectBody<List<Category>>()
            .consumeWith {
                // then
                val list = it.responseBody
                it.status shouldBe HttpStatus.OK
                list?.size shouldBe 2
                list?.find { it.id == givenCategory1.id }?.name shouldBe givenCategory1.name
                list?.find { it.id == givenCategory2.id }?.name shouldBe givenCategory2.name
            }
    }


    @Test
    fun `부모 카테고리 등록 204 성공`() {
        // given
        val givenChild = runBlocking { repository.create("TestCategory1") }
        val givenParent = runBlocking { repository.create("TestCategory2") }
        val request = PostCategoryRelationshipRequest(parentId = givenParent.id, id = givenChild.id)

        // when
        webTestClient.post()
            .uri("/categories/relationships")
            .bodyValue(request)
            .exchange()
            .expectBody()
            .consumeWith {
                // then
                it.status shouldBe HttpStatus.NO_CONTENT
            }
    }

    @Test
    fun `부모 카테고리 순환참조 방지 409 conflict`() {
        // given
        val givenChild = runBlocking { repository.create("TestCategory1") }
        val givenParent = runBlocking { repository.create("TestCategory2") }

        val request1 = PostCategoryRelationshipRequest(parentId = givenParent.id, id = givenChild.id)
        val request2 = PostCategoryRelationshipRequest(parentId = givenChild.id, id = givenParent.id)

        // when
        webTestClient.post()
            .uri("/categories/relationships")
            .bodyValue(request1)
            .exchange()
            .expectBody()
            .consumeWith {
                // then
                it.status shouldBe HttpStatus.NO_CONTENT
            }

        // when
        webTestClient.post()
            .uri("/categories/relationships")
            .bodyValue(request2)
            .exchange()
            .expectBody()
            .consumeWith {
                // then
                it.status shouldBe HttpStatus.CONFLICT
            }
    }


    @Test
    fun `카테고리 리소스 부모 카테고리 삭제 204 성공`() {
        // given
        val givenChild = runBlocking { repository.create("TestCategory1") }
        val givenParent = runBlocking { repository.create("TestCategory2") }

        PostCategoryRelationshipRequest(parentId = givenParent.id, id = givenChild.id)

        // when
        webTestClient.delete()
            .uri("/categories/${givenParent.id}")
            .exchange()
            .expectBody()
            .consumeWith {
                // then
                it.status shouldBe HttpStatus.NO_CONTENT
                runBlocking {  repository.findWithChild(givenParent.id) }.size shouldBe 0
            }
    }

    @Test
    fun `카테고리 리소스 자식 카테고리 삭제 204 성공`() {
        // given
        val givenChild = runBlocking { repository.create("TestCategory1") }
        val givenParent = runBlocking { repository.create("TestCategory2") }

        PostCategoryRelationshipRequest(parentId = givenParent.id, id = givenChild.id)

        // when
        webTestClient.delete()
            .uri("/categories/${givenChild.id}")
            .exchange()
            .expectBody()
            .consumeWith {
                // then
                it.status shouldBe HttpStatus.NO_CONTENT
                runBlocking {  repository.findWithChild(givenParent.id) }.size shouldBe 1
            }
    }

    @Test
    fun `카테고리 리소스 404 NOT FOUND 삭제할 카테고리가 존재하지 않음`() {
        // given
        val randomId = randomAlphanumericString(32)

        // when
        webTestClient.delete()
            .uri("/categories/$randomId")
            .exchange()
            .expectBody()
            .consumeWith {
                // then
                it.status shouldBe HttpStatus.NOT_FOUND
            }
    }

}