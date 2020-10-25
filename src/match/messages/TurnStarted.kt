package com.somegame.match.messages

import com.somegame.match.matchmaking.Match

data class TurnStarted(val matchSnapshot: Match.MatchSnapshot) : Message
