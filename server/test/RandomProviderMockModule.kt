package com.somegame

import com.somegame.match.RandomProvider
import com.somegame.match.player.Player
import io.mockk.every
import io.mockk.mockk
import org.koin.dsl.module

val randomProviderMockModule = module {
    val randomProvider = mockk<RandomProvider>()
    every { randomProvider.nextDouble(any(), any()) } returns 1.0

    every { randomProvider.getRandomPlayer(any()) } answers { firstArg<List<Player>>().first() }

    single { randomProvider }
}
