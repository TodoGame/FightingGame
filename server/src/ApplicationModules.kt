package com.somegame

import com.somegame.items.ItemRepository
import com.somegame.user.UserRepository
import com.somegame.user.service.UserService
import org.koin.dsl.module

val databaseRepositoryModule = module {
    single { UserRepository() }
    single { ItemRepository() }
}

val applicationModule = module {
    single { UserService() }
}
