package com.assignment.ktserver

import com.assignment.ktserver.entity.CategoryEntity
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FreeSpec

class CategorySpec : FreeSpec({
    "카테고리 이름 유효성 검증" - {
        "유효하지 않은 케이스" {
            CategoryEntity.validateName("!@ab39").shouldBeLeft()
            CategoryEntity.validateName("한글").shouldBeLeft()
            CategoryEntity.validateName("-_<>").shouldBeLeft()
            CategoryEntity.validateName(" \n").shouldBeLeft()
        }

        "유효한 케이스" {
            CategoryEntity.validateName("12345").shouldBeRight()
            CategoryEntity.validateName("abcde").shouldBeRight()
            CategoryEntity.validateName("XYZ").shouldBeRight()
            CategoryEntity.validateName("abc123").shouldBeRight()
            CategoryEntity.validateName("1q2w3e4r5t").shouldBeRight()
        }
    }
})
