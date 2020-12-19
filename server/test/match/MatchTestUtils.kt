package com.somegame.match

import match.*
import user.Username

object MatchTestUtils {
    fun getActivePlayerUsernameFromLog(log: List<Message>): Username =
        log.filterIsInstance<TurnStarted>().first().matchSnapshot.players.find { it.isActive }?.username!!
}
