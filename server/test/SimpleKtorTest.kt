package com.somegame

import com.somegame.user.repository.MockUserRepositoryFactory
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.dsl.module

@ExtendWith(MockKExtension::class)
open class SimpleKtorTest : BaseKtorTest() {
    protected var userRepository = MockUserRepositoryFactory.create()

    override var applicationModules = makeApplicationModules()

    private fun makeRepositoriesModule() = module {
        single { userRepository }
    }

    private fun makeApplicationModules() = listOf(
        module {
            single { userRepository }
        },
        applicationModule
    )

    @BeforeEach
    fun clearRepositories() {
        userRepository = MockUserRepositoryFactory.create()
        applicationModules = makeApplicationModules()
    }
}
