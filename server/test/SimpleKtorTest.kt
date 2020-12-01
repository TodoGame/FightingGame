package com.somegame

import com.somegame.items.repository.createMockItemRepository
import com.somegame.user.createMockUserRepository
import createMockFacultyRepository
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.dsl.module

@ExtendWith(MockKExtension::class)
open class SimpleKtorTest : BaseKtorTest() {
    protected var userRepository = createMockUserRepository()
    protected var itemRepository = createMockItemRepository()
    protected var facultyRepository = createMockFacultyRepository()

    override var applicationModules = makeApplicationModules()

    private fun makeApplicationModules() = listOf(
        module {
            single { userRepository }
            single { itemRepository }
            single { facultyRepository }
        }
    )

    @BeforeEach
    fun clearRepositories() {
        userRepository = createMockUserRepository()
        itemRepository = createMockItemRepository()
        facultyRepository = createMockFacultyRepository()
        applicationModules = makeApplicationModules()
    }
}
