package com.somegame

import com.somegame.items.repository.ItemRepository
import com.somegame.items.repository.ItemRepositoryImpl
import com.somegame.user.repository.UserRepository
import com.somegame.user.repository.UserRepositoryImpl
import com.somegame.user.service.UserService
import org.koin.dsl.module

val databaseRepositoryModule = module {
    single<UserRepository> { UserRepositoryImpl() }
    single<ItemRepository> { ItemRepositoryImpl() }
}

val applicationModule = module {
    single { UserService() }
}
