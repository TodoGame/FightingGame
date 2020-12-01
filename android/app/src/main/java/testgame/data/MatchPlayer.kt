package testgame.data

import user.Username

data class MatchPlayer(val username: Username, val maxHealth : Int, var currentHealth: Int)