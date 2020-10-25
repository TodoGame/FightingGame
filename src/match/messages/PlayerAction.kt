package com.somegame.match.messages

import kotlinx.serialization.Serializable

@Serializable
data class PlayerAction(val target: String, val attacker: String) : Message
