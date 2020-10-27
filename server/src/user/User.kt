package com.somegame.user

import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.auth.*

data class User(
    val username: String,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var password: String,
    var name: String,
) : Principal {

    override fun toString() = "User(username=$username)"
}
