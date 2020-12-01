package com.somegame.security

import com.somegame.BaseKoinTest
import com.somegame.responseExceptions.BadRequestException
import com.somegame.responseExceptions.ConflictException
import com.somegame.responseExceptions.UnauthorizedException
import com.somegame.user.publicData
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.inject
import security.UserLoginInput
import security.UserRegisterInput
import testFaculty1
import user.UserData

internal class SecurityRoutingHelpersTest : BaseKoinTest() {
    private var securityRoutingHelpers = SecurityRoutingHelpers()

    @BeforeEach
    fun clean() {
        securityRoutingHelpers = SecurityRoutingHelpers()
    }

    @Test
    fun `registerUser should return the user that is stored in the repository`() {
        val input = UserRegisterInput("username", "password", "name", testFaculty1.getId())
        val registeredUser = securityRoutingHelpers.registerUser(input)
        val user = userRepository.findUserByUsername("username")
        assertEquals(registeredUser, user)
    }

    @Test
    fun `register should throw if user already exists`() {
        val input = UserRegisterInput("username", "password", "name", testFaculty1.getId())
        val inputWithTheSameUsername = UserRegisterInput("username", "pass", "othername", testFaculty1.getId())

        securityRoutingHelpers.registerUser(input)
        assertThrows(ConflictException::class.java) {
            securityRoutingHelpers.registerUser(inputWithTheSameUsername)
        }
    }

    @Test
    fun `should login into registered users if password is correct`() {
        val input = UserRegisterInput("username", "password", "name", testFaculty1.getId())
        val registeredUser = securityRoutingHelpers.registerUser(input)
        val user = securityRoutingHelpers.loginUser(UserLoginInput("username", "password"))

        assertEquals(registeredUser, user)
    }

    @Test
    fun `should throw if trying to login but no users exist`() {
        assertThrows(UnauthorizedException::class.java) {
            securityRoutingHelpers.loginUser(UserLoginInput("username", "password"))
        }
    }

    @Test
    fun `should throw if trying to login with username that does not exist`() {
        val input = UserRegisterInput("username", "password", "name", testFaculty1.getId())
        securityRoutingHelpers.registerUser(input)
        assertThrows(UnauthorizedException::class.java) {
            securityRoutingHelpers.loginUser(UserLoginInput("otherUsername", "password"))
        }
    }

    @Test
    fun `should throw if trying to login with correct username but incorrect password`() {
        val input = UserRegisterInput("username", "password", "name", testFaculty1.getId())
        securityRoutingHelpers.registerUser(input)
        assertThrows(UnauthorizedException::class.java) {
            securityRoutingHelpers.loginUser(UserLoginInput("username", "incorrectPassword"))
        }
    }
}
