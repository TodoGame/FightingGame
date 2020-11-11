package com.somegame

import com.somegame.user.repository.MockUserRepository
import com.somegame.user.repository.UserRepository
import org.koin.dsl.module

val mockRepositoryModule = module {
    single<UserRepository> { MockUserRepository() }
}
