package com.somegame

import com.somegame.user.repository.MockUserRepositoryFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

open class BaseKoinTest : KoinTest {
    protected open var userRepository = MockUserRepositoryFactory.create()

    private var userRepositoryModule = makeRepositoriesModule()

    private fun makeRepositoriesModule() = module {
        single { userRepository }
    }

    @BeforeEach
    fun clearRepositories() {
        userRepository = MockUserRepositoryFactory.create()
        userRepositoryModule = makeRepositoriesModule()
        startKoin {
            modules(userRepositoryModule, applicationModule)
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
}
