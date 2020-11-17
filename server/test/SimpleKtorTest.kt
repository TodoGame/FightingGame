package com.somegame

import com.somegame.user.repository.MockUserRepository
import com.somegame.user.repository.UserRepository
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.dsl.module

@ExtendWith(MockKExtension::class)
open class SimpleKtorTest : BaseKtorTest() {
    protected var userRepository = MockUserRepository()

    private val repositoriesModule = module {
        single<UserRepository> { userRepository }
    }

    override val applicationModules = listOf(repositoriesModule, applicationModule)

    @BeforeEach
    fun clearRepositories() {
        userRepository.clear()
    }
}
