package com.somegame.user.service

import com.somegame.faculty.FacultyRepository
import com.somegame.responseExceptions.UnauthorizedException
import com.somegame.user.User
import com.somegame.user.UserRepository
import org.koin.core.KoinComponent
import org.koin.core.inject
import security.UserLoginInput
import security.UserRegisterInput
import user.Username

class UserService : KoinComponent {
    private val facultyRepository: FacultyRepository by inject()
    private val userRepository: UserRepository by inject()

    fun findUserByUsername(username: Username) = userRepository.findUserByUsername(username)

    fun registerUser(input: UserRegisterInput): User {
        if (findUserByUsername(input.username) != null) {
            throw UserAlreadyExistsException(input.username)
        }
        return userRepository.createUser(input)
    }

    fun loginUser(input: UserLoginInput): User {
        val user = findUserByUsername(input.username)
        if (user != null && user.password == input.password) {
            return user
        } else {
            throw InvalidLoginInputException(input)
        }
    }

    class UserNotFoundException(username: Username) :
        UnauthorizedException("User with username=$username does not exist")

    class UserAlreadyExistsException(username: Username) :
        IllegalArgumentException("User with username $username already exists")

    class InvalidLoginInputException(input: UserLoginInput) : UnauthorizedException("User input $input is not valid")
}
