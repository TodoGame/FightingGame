package com.somegame

import com.somegame.items.repository.MockItemRepositoryFactory
import com.somegame.user.repository.MockUserRepositoryFactory
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.dsl.module

@ExtendWith(MockKExtension::class)
open class SimpleKtorTest : BaseKtorTest() {
    protected var userRepository = MockUserRepositoryFactory.create()
    protected var itemRepository = MockItemRepositoryFactory.create()

    override var applicationModules = makeApplicationModules()

    private fun makeApplicationModules() = listOf(
        module {
            single { userRepository }
            single { itemRepository }
        },
        applicationModule
    )

    @BeforeEach
    fun clearRepositories() {
        userRepository = MockUserRepositoryFactory.create()
        itemRepository = MockItemRepositoryFactory.create()
        applicationModules = makeApplicationModules()
    }
}
