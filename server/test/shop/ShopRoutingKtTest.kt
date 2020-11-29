package shop

import com.somegame.SimpleKtorTest
import com.somegame.TestUtils.addJsonContentHeader
import com.somegame.TestUtils.addJwtHeader
import com.somegame.shop.shop
import com.somegame.user.repository.MockUserRepositoryFactory.makeNewTestUser
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShopRoutingKtTest : SimpleKtorTest() {
    private fun withApp(block: TestApplicationEngine.() -> Unit) = withBaseApp({
        routing {
            shop()
        }
    }) { block() }

    @Test
    fun `should respond with BadRequest if sent gibberish instead of item id`() = withApp {
        handleRequest {
            uri = ShopEndpoints.BUY_ITEM_ENDPOINT
            method = HttpMethod.Post
            addJwtHeader("user1")
            addJsonContentHeader()
            setBody("sdsds")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun `should respond with 404 if item with this id does not exist`() = withApp {
        handleRequest {
            uri = ShopEndpoints.BUY_ITEM_ENDPOINT
            method = HttpMethod.Post
            addJwtHeader("user1")
            addJsonContentHeader()
            setBody("100")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }

    @Test
    fun `should respond with Forbidden if not enough money`() = withApp {
        userRepository.makeNewTestUser("brokeUser")
        handleRequest {
            uri = ShopEndpoints.BUY_ITEM_ENDPOINT
            method = HttpMethod.Post
            addJwtHeader("brokeUser")
            addJsonContentHeader()
            setBody("1")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.Forbidden, response.status())
        }
    }

    @Test
    fun `should respond with OK if user has enough money`() = withApp {
        val user = userRepository.makeNewTestUser("testUser")
        user.acceptMoney(10)
        handleRequest {
            uri = ShopEndpoints.BUY_ITEM_ENDPOINT
            method = HttpMethod.Post
            addJwtHeader("testUser")
            addJsonContentHeader()
            setBody("2")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `after buying user should have item1`() = withApp {
        val user = userRepository.makeNewTestUser("testUser")
        user.acceptMoney(10)
        handleRequest {
            uri = ShopEndpoints.BUY_ITEM_ENDPOINT
            method = HttpMethod.Post
            addJwtHeader("testUser")
            addJsonContentHeader()
            setBody("1")
        }
        val boughtItem = itemRepository.getItemById(1)
        assert(user.hasItem(boughtItem!!))
    }

    @Test
    fun `after buying user should have item2`() = withApp {
        val user = userRepository.makeNewTestUser("testUser")
        user.acceptMoney(10)
        handleRequest {
            uri = ShopEndpoints.BUY_ITEM_ENDPOINT
            method = HttpMethod.Post
            addJwtHeader("testUser")
            addJsonContentHeader()
            setBody("2")
        }
        val boughtItem = itemRepository.getItemById(2)
        assert(user.hasItem(boughtItem!!))
    }

    @Test
    fun `should respond with Conflict if user attempts to buy the same item twice`() = withApp {
        val user = userRepository.makeNewTestUser("testUser")
        user.acceptMoney(10)
        handleRequest {
            uri = ShopEndpoints.BUY_ITEM_ENDPOINT
            method = HttpMethod.Post
            addJwtHeader("testUser")
            addJsonContentHeader()
            setBody("1")
        }
        handleRequest {
            uri = ShopEndpoints.BUY_ITEM_ENDPOINT
            method = HttpMethod.Post
            addJwtHeader("testUser")
            addJsonContentHeader()
            setBody("1")
        }.apply {
            assert(requestHandled)
            assertEquals(HttpStatusCode.Conflict, response.status())
        }
    }
}