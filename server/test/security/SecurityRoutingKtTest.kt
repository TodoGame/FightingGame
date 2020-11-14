package security

import com.somegame.SimpleKtorTest
import com.somegame.TestUtils.addJsonContentHeader
import com.somegame.security.SecurityRouting.security
import com.somegame.user.service.UserService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.koin.core.inject
import user.UserData

internal class SecurityRoutingKtTest : SimpleKtorTest() {
    private fun withApp(block: TestApplicationEngine.() -> Unit): Unit = withBaseApp(
        {
            routing {
                security()
            }
        },
        block
    )

    @Test
    fun `register should respond with Bad Request to gibberish`() = withApp {
        handleRequest {
            uri = REGISTER_ENDPOINT
            method = HttpMethod.Post
            setBody("Some goddamn gibberish lol")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun `register should respond with Bad Request to gibberish with json content type`() = withApp {
        handleRequest {
            uri = REGISTER_ENDPOINT
            method = HttpMethod.Post
            setBody("Some goddamn gibberish lol")
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun `register should respond with Created if sent correct input`() = withApp {
        val username = "username"
        val password = "password"
        val name = "name"
        val registerInput = UserRegisterInput(username, password, name)
        handleRequest {
            uri = REGISTER_ENDPOINT
            method = HttpMethod.Post
            setBody(Json.encodeToString(registerInput))
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.Created, response.status())
        }
    }

    @Test
    fun `register should respond with registered userdata`() = withApp {
        val username = "username"
        val password = "password"
        val name = "name"
        val registerInput = UserRegisterInput(username, password, name)
        val expectedUserData = UserData(username, name)
        handleRequest {
            uri = REGISTER_ENDPOINT
            method = HttpMethod.Post
            setBody(Json.encodeToString(registerInput))
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val userData = response.content?.let { Json.decodeFromString<UserData>(it) }
            assertEquals(expectedUserData, userData)
        }
    }

    @Test
    fun `register should respond with Authorization header with Bearer token if valid input`() = withApp {
        val username = "username"
        val password = "password"
        val name = "name"
        val registerInput = UserRegisterInput(username, password, name)
        handleRequest {
            uri = REGISTER_ENDPOINT
            method = HttpMethod.Post
            setBody(Json.encodeToString(registerInput))
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val authHeaderValue = response.headers["Authorization"]
            assertNotNull(authHeaderValue)
            assert(authHeaderValue?.startsWith("Bearer") ?: false)
        }
    }

    @Test
    fun `login should respond with Bad Request on gibberish`() = withApp {
        handleRequest {
            uri = LOGIN_ENDPOINT
            method = HttpMethod.Post
            setBody("Some gibberish lol")
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun `login should respond with Unauthorized if user does not exist`() = withApp {
        val fakeLoginInput = UserLoginInput("fakeUser", "fakePassword")
        handleRequest {
            uri = LOGIN_ENDPOINT
            method = HttpMethod.Post
            setBody(Json.encodeToString(fakeLoginInput))
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `login should respond with Authorization header with Bearer token if user was registered before`() = withApp {
        val username = "username"
        val password = "password"
        val name = "name"
        val registerInput = UserRegisterInput(username, password, name)
        handleRequest {
            uri = REGISTER_ENDPOINT
            method = HttpMethod.Post
            setBody(Json.encodeToString(registerInput))
            addJsonContentHeader()
        }
        val loginInput = UserLoginInput(username, password)
        handleRequest {
            uri = LOGIN_ENDPOINT
            method = HttpMethod.Post
            setBody(Json.encodeToString(loginInput))
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val authHeaderValue = response.headers["Authorization"]
            assertNotNull(authHeaderValue)
            assert(authHeaderValue?.startsWith("Bearer") ?: false)
        }
    }

    @Test
    fun `login should respond with Authorization header with Bearer token if user was registered (even directly using UserService)`() =
        withApp {
            val userService: UserService by inject()
            val username = "username"
            val password = "password"
            val name = "name"

            userService.registerUser(UserRegisterInput(username, password, name))

            val loginInput = UserLoginInput(username, password)

            handleRequest {
                uri = LOGIN_ENDPOINT
                method = HttpMethod.Post
                setBody(Json.encodeToString(loginInput))
                addJsonContentHeader()
            }.apply {
                assert(requestHandled) { "Request not handled" }
                val authHeaderValue = response.headers["Authorization"]
                assertNotNull(authHeaderValue)
                assert(authHeaderValue?.startsWith("Bearer") ?: false)
            }
        }
}