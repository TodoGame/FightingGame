package com.somegame

import com.somegame.user.repository.UserRepository
import com.somegame.user.repository.UserRepositoryImpl
import com.somegame.user.service.UserService
import org.koin.dsl.module

val databaseRepositoryModule = module {
    single<UserRepository> { UserRepositoryImpl() }
}

val applicationModule = module {
    single { UserService(get()) }
}
