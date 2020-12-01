package com.somegame.security

import io.ktor.auth.*
import user.Username

data class UserPrincipal(val username: Username) : Principal
