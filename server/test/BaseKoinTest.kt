package com.somegame

import com.somegame.faculty.FacultyPointsManager
import com.somegame.user.UserMoneyManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import user.Username

open class BaseKoinTest : KoinTest {
    private val repositoriesMock = RepositoriesMock()

    protected val userRepository
        get() = repositoriesMock.userRepository

    protected val itemRepository
        get() = repositoriesMock.itemRepository

    protected val facultyRepository
        get() = repositoriesMock.facultyRepository

    private var repositoriesModule = repositoriesMock.repositoriesModule

    protected val testItem1
        get() = itemRepository.testItem1()

    protected val testItem2
        get() = itemRepository.testItem2()

    protected val testFaculty1
        get() = facultyRepository.testFaculty1()

    protected val testFaculty2
        get() = facultyRepository.testFaculty2()

    protected val user1
        get() = userRepository.user1()

    protected val user2
        get() = userRepository.user2()

    fun makeNewTestUser(username: Username) = repositoriesMock.makeNewTestUser(username)

    @BeforeEach
    fun clearRepositories() {
        repositoriesMock.clear()
        val managersModule = module {
            single { UserMoneyManager() }
            single { FacultyPointsManager() }
        }
        startKoin {
            modules(repositoriesModule, managersModule)
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
}
