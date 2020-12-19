package com.somegame

import com.somegame.faculty.FacultyPointsManager
import com.somegame.faculty.FacultyRepository
import com.somegame.items.ItemRepository
import com.somegame.match.RandomProvider
import com.somegame.user.UserMoneyManager
import com.somegame.user.UserRepository
import org.koin.dsl.module

val databaseRepositoryModule = module {
    single { UserRepository() }
    single { ItemRepository() }
    single { FacultyRepository() }
}

val otherModule = module {
    single { UserMoneyManager() }
    single { FacultyPointsManager() }
    single { RandomProvider() }
}
