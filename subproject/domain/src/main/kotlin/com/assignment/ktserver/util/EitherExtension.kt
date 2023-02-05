package com.assignment.ktserver.util

import arrow.core.Either

fun <A> A.leftOrUnit(): Either<A, Unit> = Either.Left(this)
