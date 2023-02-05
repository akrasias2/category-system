package com.assignment.ktserver

import com.assignment.ktserver.api.CategoryApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class ApiRouter(val categoryApi: CategoryApi) {

    @Bean
    fun requestRouter() = coRouter {
        "/categories".nest {
            GET("/{id}", categoryApi::getCategory) // return specific category
            GET("", categoryApi::getCategories) // return list of category
            POST("", categoryApi::postCategories) // create new category
            POST("/relationships", categoryApi::postRelationship) // connect category parent-child relationship
            PATCH("/{id}", categoryApi::patchCategory) // update specific category
            DELETE("/{id}", categoryApi::deleteCategory) // delete list of category
        }
    }
}
