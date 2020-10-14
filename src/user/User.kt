package com.somegame.user

import io.ktor.auth.*

data class User(val username: String, val password: String) : Principal
