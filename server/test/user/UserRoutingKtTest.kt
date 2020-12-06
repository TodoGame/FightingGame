package user

import com.somegame.SimpleKtorTest
import com.somegame.TestUtils.addJwtHeader
import com.somegame.user.publicData
import com.somegame.user.user
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UserRoutingKtTest : SimpleKtorTest() {
    private fun withApp(block: TestApplicationEngine.() -> Unit): Unit = withBaseApp(
        {
            routing {
                user()
            }
        },
        block
    )

    @Test
    fun `should respond with Unauthorized if no authorization header is provided`() = withApp {
        handleRequest {
            uri = GET_ME_ENDPOINT
            method = HttpMethod.Get
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `should respond with Unauthorized if incorrect authorization token provided`() = withApp {
        handleRequest {
            uri = GET_ME_ENDPOINT
            method = HttpMethod.Get
            addHeader("Authorization", "Bearer sadgsadasda")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `should respond with user public data if provided with correct jwt token`() = withApp {
        handleRequest {
            uri = GET_ME_ENDPOINT
            method = HttpMethod.Get
            addJwtHeader("user1")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.OK, response.status())
            val userData = response.content?.let { Json.decodeFromString<UserData>(it) }
            assertEquals(user1.publicData(), userData)
        }
    }

    @Test
    fun `getUser should respond with user1 if given username=user1`() = withApp {
        handleRequest {
            uri = "$GET_USER_ENDPOINT?username=user1"
            method = HttpMethod.Get
            addJwtHeader("user1")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val userData = response.content?.let { Json.decodeFromString<UserData>(it) }
            assertEquals(user1.publicData(), userData)
        }
    }

    @Test
    fun `getUser should respond with user2 if given username=user2`() = withApp {
        handleRequest {
            uri = "$GET_USER_ENDPOINT?username=user2"
            method = HttpMethod.Get
            addJwtHeader("user1")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val userData = response.content?.let { Json.decodeFromString<UserData>(it) }
            assertEquals(user2.publicData(), userData)
        }
    }

    @Test
    fun `getUser should respond with Bad Request if not given username`() = withApp {
        handleRequest {
            uri = GET_USER_ENDPOINT
            method = HttpMethod.Get
            addJwtHeader("user1")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun `getUser should respond with 404 if given username=fakeUsername`() = withApp {
        handleRequest {
            uri = "$GET_USER_ENDPOINT?username=fakeUsername"
            method = HttpMethod.Get
            addJwtHeader("user1")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }
}
