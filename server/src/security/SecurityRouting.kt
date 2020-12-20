package com.somegame.security

import com.somegame.addJwtToken
import com.somegame.faculty.FacultyNotFound
import com.somegame.faculty.FacultyRepository
import com.somegame.handleReceiveExceptions
import com.somegame.responseExceptions.BadRequestException
import com.somegame.responseExceptions.ConflictException
import com.somegame.responseExceptions.UnauthorizedException
import com.somegame.user.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.apache.commons.codec.digest.DigestUtils
import org.koin.core.KoinComponent
import org.koin.core.inject
import security.*
import user.USER_MAX_NAME_LENGTH
import user.USER_MAX_USERNAME_LENGTH
import user.Username

fun Routing.security() {
    val securityRoutingHelpers = SecurityRoutingHelpers()

    post(LOGIN_ENDPOINT) {
        val input = handleReceiveExceptions { call.receive<UserLoginInput>() }
        val user = securityRoutingHelpers.loginUser(input)

        addJwtToken(user)
        call.respond(user.publicData())
    }

    post(REGISTER_ENDPOINT) {
        val input = handleReceiveExceptions { call.receive<UserRegisterInput>() }

        val newUser = securityRoutingHelpers.registerUser(input)

        addJwtToken(newUser)
        call.response.status(HttpStatusCode.Created)
        call.respond(newUser.publicData())
    }
}

class SecurityRoutingHelpers : KoinComponent {
    private val userRepository: UserRepository by inject()
    private val facultyRepository: FacultyRepository by inject()

    fun loginUser(input: UserLoginInput): User {
        val user = userRepository.findUserByUsername(input.username)
        if (user != null && user.password == hash(input.password)) {
            return user
        } else {
            throw UnauthorizedException()
        }
    }

    fun registerUser(input: UserRegisterInput): User {
        validateLengths(input)
        if (userRepository.doesUserExist(input.username)) {
            throw UserAlreadyExistsException(input.username)
        }
        val faculty = facultyRepository.getFacultyById(input.facultyId) ?: throw FacultyNotFound(input.facultyId)
        return userRepository.createUser(
            username = input.username,
            password = hash(input.password),
            name = input.name,
            faculty = faculty
        )
    }

    private fun validateLengths(input: UserRegisterInput) {
        if (input.username.length > USER_MAX_USERNAME_LENGTH) {
            throw BadRequestException("Username exceeds max length of $USER_MAX_NAME_LENGTH")
        }
        if (input.name.length > USER_MAX_NAME_LENGTH) {
            throw BadRequestException("Name exceeds max length of $USER_MAX_NAME_LENGTH")
        }
    }

    private fun hash(string: String): String = DigestUtils.sha256Hex(string)

    class UserAlreadyExistsException(username: Username) :
        ConflictException("User with username $username already exists")
}
