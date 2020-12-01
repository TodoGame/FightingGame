package com.somegame

import createMockFacultyRepository
import com.somegame.items.repository.createMockItemRepository
import com.somegame.user.createMockUserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

open class BaseKoinTest : KoinTest {
    protected open var userRepository = createMockUserRepository()
    protected var itemRepository = createMockItemRepository()
    protected var facultyRepository = createMockFacultyRepository()

    private var repositoriesModule = makeRepositoriesModule()

    private fun makeRepositoriesModule() = module {
        single { userRepository }
        single { itemRepository }
        single { facultyRepository }
    }

    @BeforeEach
    fun clearRepositories() {
        userRepository = createMockUserRepository()
        itemRepository = createMockItemRepository()
        facultyRepository = createMockFacultyRepository()
        repositoriesModule = makeRepositoriesModule()
        startKoin {
            modules(repositoriesModule)
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
}
