package com.somegame.items.repository

import com.somegame.SimpleKtorTest
import com.somegame.TestUtils.addJsonContentHeader
import com.somegame.TestUtils.addJwtHeader
import com.somegame.items.getAllPublicItemData
import com.somegame.items.publicData
import com.somegame.shop.shop
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import item.ItemData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import shop.ShopEndpoints

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
        makeNewTestUser("brokeUser")
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
        val user = makeNewTestUser("testUser")
        user.acceptMoney(1000)
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
        val user = makeNewTestUser("testUser")
        user.acceptMoney(1000)
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
        val user = makeNewTestUser("testUser")
        user.acceptMoney(1000)
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
        val user = makeNewTestUser("testUser")
        user.acceptMoney(1000)
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
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.Conflict, response.status())
        }
    }

    @Test
    fun `getAllItems should respond with all test items`() = withApp {
        handleRequest {
            uri = ShopEndpoints.GET_ALL_ITEMS_ENDPOINT
            method = HttpMethod.Get
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val items = response.content?.let { Json.decodeFromString<List<ItemData>>(it) }
            assertEquals(itemRepository.getAllPublicItemData(), items)
        }
    }

    @Test
    fun `getItem with id=1 should respond with test item 1`() = withApp {
        handleRequest {
            uri = "${ShopEndpoints.GET_ITEM_ENDPOINT}?id=1"
            method = HttpMethod.Get
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val item = response.content?.let { Json.decodeFromString<ItemData>(it) }
            assertEquals(testItem1.publicData(), item)
        }
    }

    @Test
    fun `getItem with id=2 should respond with test item 2`() = withApp {
        handleRequest {
            uri = "${ShopEndpoints.GET_ITEM_ENDPOINT}?id=2"
            method = HttpMethod.Get
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val item = response.content?.let { Json.decodeFromString<ItemData>(it) }
            assertEquals(testItem2.publicData(), item)
        }
    }

    @Test
    fun `getItem with id=100 should respond with 404`() = withApp {
        handleRequest {
            uri = "${ShopEndpoints.GET_ITEM_ENDPOINT}?id=100"
            method = HttpMethod.Get
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }

    @Test
    fun `getItem with id=stringValue should respond with BadRequest`() = withApp {
        handleRequest {
            uri = "${ShopEndpoints.GET_ITEM_ENDPOINT}?id=stringValue"
            method = HttpMethod.Get
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun `getNotOwnedItems should initially return all items`() = withApp {
        handleRequest {
            uri = ShopEndpoints.GET_NOT_OWNED_ITEMS
            method = HttpMethod.Get
            addJwtHeader("user1")
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val items = response.content?.let { Json.decodeFromString<List<ItemData>>(it) }
            assertEquals(itemRepository.getAllPublicItemData(), items)
        }
    }

    @Test
    fun `getNotOwnedItems should respond with list that does not contain first item if it is bought`() = withApp {
        val user = makeNewTestUser("richUser")
        val itemId = 1
        user.acceptMoney(1000)
        handleRequest {
            uri = ShopEndpoints.BUY_ITEM_ENDPOINT
            method = HttpMethod.Post
            addJwtHeader("richUser")
            addJsonContentHeader()
            setBody(itemId.toString())
        }
        handleRequest {
            uri = ShopEndpoints.GET_NOT_OWNED_ITEMS
            method = HttpMethod.Get
            addJwtHeader("richUser")
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val items = response.content?.let { Json.decodeFromString<List<ItemData>>(it) }
            val item = itemRepository.getItemById(itemId)!!
            assertNotNull(items)
            if (items != null) {
                assert(!items.contains(item.publicData()))
            }
        }
    }

    @Test
    fun `getNotOwnedItems should respond with list that does not contain the second item if it is bought`() = withApp {
        val user = makeNewTestUser("richUser")
        val itemId = 2
        user.acceptMoney(1000)
        handleRequest {
            uri = ShopEndpoints.BUY_ITEM_ENDPOINT
            method = HttpMethod.Post
            addJwtHeader("richUser")
            addJsonContentHeader()
            setBody(itemId.toString())
        }
        handleRequest {
            uri = ShopEndpoints.GET_NOT_OWNED_ITEMS
            method = HttpMethod.Get
            addJwtHeader("richUser")
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val items = response.content?.let { Json.decodeFromString<List<ItemData>>(it) }
            val item = itemRepository.getItemById(itemId)!!
            assertNotNull(items)
            if (items != null) {
                assert(!items.contains(item.publicData()))
            }
        }
    }

    @Test
    fun `getNotOwnedItems should respond with empty list if all items are bought`() = withApp {
        val user = makeNewTestUser("richUser")
        user.acceptMoney(10000)
        for (i in 1..4) {
            handleRequest {
                uri = ShopEndpoints.BUY_ITEM_ENDPOINT
                method = HttpMethod.Post
                addJwtHeader("richUser")
                addJsonContentHeader()
                setBody(i.toString())
            }
        }
        handleRequest {
            uri = ShopEndpoints.GET_NOT_OWNED_ITEMS
            method = HttpMethod.Get
            addJwtHeader("richUser")
            addJsonContentHeader()
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val items = response.content?.let { Json.decodeFromString<List<ItemData>>(it) }
            assertNotNull(items)
            assertEquals(listOf<ItemData>(), items)
        }
    }
}
