package com.somegame.match.messages

import kotlinx.serialization.Serializable

@Serializable
data class MatchJoined(val players: List<String>) : Message
