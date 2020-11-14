package com.somegame.user.service

import com.somegame.security.UnauthorizedException
import com.somegame.user.repository.UserEntity
import com.somegame.user.repository.UserRepository
import org.koin.core.KoinComponent
import org.koin.core.inject
import security.UserLoginInput
import security.UserRegisterInput
import user.Username

class UserService : KoinComponent {
    private val userRepository: UserRepository by inject()

    fun findUserByUsername(username: Username) = userRepository.findUserByUsername(username)

    fun registerUser(input: UserRegisterInput): UserEntity {
        if (findUserByUsername(input.username) != null) {
            throw UserAlreadyExistsException(input.username)
        }
        return userRepository.createUser(input)
    }

    fun loginUser(input: UserLoginInput): UserEntity {
        val user = findUserByUsername(input.username)
        if (user != null && user.password == input.password) {
            return user
        } else {
            throw InvalidLoginInputException(input)
        }
    }

    class UserAlreadyExistsException(username: Username) : IllegalArgumentException("User with username $username already exists")

    class InvalidLoginInputException(input: UserLoginInput) : UnauthorizedException("User input $input is not valid")
}
