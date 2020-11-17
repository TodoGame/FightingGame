package com.somegame.match

import match.*
import user.Username

object MatchTestUtils {
    fun generateActivePlayerLog(activeUsername: Username, passiveUsername: Username) = listOf(
        MatchStarted(setOf(activeUsername, passiveUsername)),
        TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot(activeUsername, true, 15),
                    PlayerSnapshot(passiveUsername, false, 15)
                )
            )
        ),
        TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot(activeUsername, false, 15),
                    PlayerSnapshot(passiveUsername, true, 5)
                )
            )
        ),
        PlayerAction(activeUsername, passiveUsername),
        TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot(activeUsername, true, 5),
                    PlayerSnapshot(passiveUsername, false, 5)
                )
            )
        ),
        MatchEnded(activeUsername)
    )

    fun generatePassivePlayerLog(activeUsername: Username, passiveUsername: Username) = listOf(
        MatchStarted(setOf(activeUsername, passiveUsername)),
        TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot(activeUsername, true, 15),
                    PlayerSnapshot(passiveUsername, false, 15)
                )
            )
        ),
        PlayerAction(passiveUsername, activeUsername),
        TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot(activeUsername, false, 15),
                    PlayerSnapshot(passiveUsername, true, 5)
                )
            )
        ),
        TurnStarted(
            MatchSnapshot(
                setOf(
                    PlayerSnapshot(activeUsername, true, 5),
                    PlayerSnapshot(passiveUsername, false, 5)
                )
            )
        ),
        PlayerAction(passiveUsername, activeUsername),
        MatchEnded(activeUsername)
    )

    fun getActivePlayerUsernameFromLog(log: List<Message>): Username =
        log.filterIsInstance<TurnStarted>().first().matchSnapshot.players.find { it.isActive }?.username!!
}
