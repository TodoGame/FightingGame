package com.somegame

import com.somegame.faculty.FacultyRepository
import com.somegame.items.ItemRepository
import com.somegame.user.UserRepository
import com.somegame.user.service.UserService
import org.koin.dsl.module

val databaseRepositoryModule = module {
    single { UserRepository() }
    single { ItemRepository() }
    single { FacultyRepository() }
}

val applicationModule = module {
    single { UserService() }
}
