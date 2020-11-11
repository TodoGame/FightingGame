package com.somegame

import com.somegame.user.repository.MockUserRepository
import com.somegame.user.repository.UserRepository
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseKoinTest : KoinComponent {
    protected val userRepository = MockUserRepository()

    private val userRepositoryModule = module {
        single<UserRepository> { userRepository }
    }

    @BeforeAll
    fun setUp() {
        startKoin {
            modules(userRepositoryModule, applicationModule)
        }
    }

    @BeforeEach
    fun clearRepositories() {
        userRepository.clear()
    }
}
