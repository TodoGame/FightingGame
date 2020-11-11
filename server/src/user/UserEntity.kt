package com.somegame.user

import com.fasterxml.jackson.annotation.JsonProperty
import com.somegame.security.UserPrincipal
import io.ktor.auth.*
import user.User

data class UserEntity(
    override val username: String,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var password: String,
    override var name: String,
) : Principal, User {

    val principal: UserPrincipal
        get() = UserPrincipal(username)

    override fun toString() = "User(username=$username)"
}
