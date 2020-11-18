package com.somegame

import com.somegame.user.repository.MockUserRepository
import com.somegame.user.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

open class BaseKoinTest : KoinTest {
    protected open val userRepository = MockUserRepository()

    private val userRepositoryModule = module {
        single<UserRepository> { userRepository }
    }

    @BeforeEach
    fun clearRepositories() {
        userRepository.clear()
        startKoin {
            modules(userRepositoryModule, applicationModule)
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
}
