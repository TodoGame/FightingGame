package user.service

import com.somegame.BaseKoinTest
import com.somegame.user.repository.MockUserRepository
import com.somegame.user.service.UserService
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.koin.core.inject
import security.UserLoginInput
import security.UserRegisterInput

internal class UserServiceTest : BaseKoinTest() {
    override val userRepository = spyk(MockUserRepository())
    private val userService: UserService by inject()

    @Test
    fun `should return registered user`() {
        val user = userService.registerUser(UserRegisterInput("username", "password", "name"))
        assertEquals("username", user.username)
        assertEquals("name", user.name)
    }

    @Test
    fun `registered users should be findable`() {
        val registeredUser = userService.registerUser(UserRegisterInput("username", "password", "name"))
        val user = userService.findUserByUsername("username")
        assertEquals(registeredUser, user)
    }

    @Test
    fun `findUserByUsername should call the corresponding userRepository's method`() {
        userService.registerUser(UserRegisterInput("username", "password", "name"))
        userService.findUserByUsername("username")
        verify {
            userRepository.findUserByUsername("username")
        }
    }

    @Test
    fun `register should call userRepository's createUser()`() {
        val input = UserRegisterInput("username", "password", "name")
        userService.registerUser(input)
        verify {
            userRepository.createUser(input)
        }
    }

    @Test
    fun `register should throw if user already exists`() {
        val input = UserRegisterInput("username", "password", "name")
        val inputWithTheSameUsername = UserRegisterInput("username", "pass", "othername")

        userService.registerUser(input)
        assertThrows(UserService.UserAlreadyExistsException::class.java) {
            userService.registerUser(inputWithTheSameUsername)
        }
    }

    @Test
    fun `should login into registered users if password is correct`() {
        val input = UserRegisterInput("username", "password", "name")
        val registeredUser = userService.registerUser(input)
        val user = userService.loginUser(UserLoginInput("username", "password"))

        assertEquals(registeredUser, user)
    }

    @Test
    fun `should throw if trying to login but no users exist`() {
        assertThrows(UserService.InvalidLoginInputException::class.java) {
            userService.loginUser(UserLoginInput("username", "password"))
        }
    }

    @Test
    fun `should throw if trying to login with username that does not exist`() {
        val input = UserRegisterInput("username", "password", "name")
        userService.registerUser(input)
        assertThrows(UserService.InvalidLoginInputException::class.java) {
            userService.loginUser(UserLoginInput("otherUsername", "password"))
        }
    }

    @Test
    fun `should throw if trying to login with correct username but incorrect password`() {
        val input = UserRegisterInput("username", "password", "name")
        userService.registerUser(input)
        assertThrows(UserService.InvalidLoginInputException::class.java) {
            userService.loginUser(UserLoginInput("username", "incorrectPassword"))
        }
    }
}
