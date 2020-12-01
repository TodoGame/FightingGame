package com.somegame.security

import com.somegame.addJwtToken
import com.somegame.faculty.FacultyNotFound
import com.somegame.faculty.FacultyRepository
import com.somegame.handleReceiveExceptions
import com.somegame.responseExceptions.ConflictException
import com.somegame.responseExceptions.UnauthorizedException
import com.somegame.user.User
import com.somegame.user.UserRepository
import com.somegame.user.doesUserExist
import com.somegame.user.publicData
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.apache.commons.codec.digest.DigestUtils
import org.koin.core.KoinComponent
import org.koin.core.inject
import security.*
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

    private fun hash(string: String): String = DigestUtils.sha256Hex(string)

    class UserAlreadyExistsException(username: Username) :
        ConflictException("User with username $username already exists")
}
