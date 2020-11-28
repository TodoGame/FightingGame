package user

import com.somegame.SimpleKtorTest
import com.somegame.TestUtils.addJwtHeader
import com.somegame.user.UserExtensions.publicData
import com.somegame.user.repository.MockUserRepositoryFactory.user1
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
            assertEquals(userRepository.user1().publicData(), userData)
        }
    }
}
