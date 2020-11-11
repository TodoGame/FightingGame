package com.somegame.user.repository

import com.somegame.security.UserPrincipal
import user.UserData
import user.Username

class MockUserEntity(override val username: Username, override val password: String, override val name: String) :
    UserEntity {
    override fun getPrincipal() = UserPrincipal(username)

    override fun getPublicData() = UserData(username, name)
}
