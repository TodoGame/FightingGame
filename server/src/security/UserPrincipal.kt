package com.somegame.security

import io.ktor.auth.*

data class UserPrincipal(val username: String) : Principal
