package testgame.network

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import item.ItemData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import shop.ShopEndpoints.BUY_ITEM_ENDPOINT
import shop.ShopEndpoints.GET_ALL_ITEMS_ENDPOINT
import shop.ShopEndpoints.GET_NOT_OWNED_ITEMS
import user.UserData

object ShopApi : NetworkService() {
    @KtorExperimentalAPI
    suspend fun buyItem(token: String, itemId: Int): UserData {
        val response =  getSuccessfulResponseOrException {
            client.post() {
                url("${BASE_HTTP_URL}$BUY_ITEM_ENDPOINT")
                header(AUTHORIZATION_HEADER_NAME, token)
                parameter(ITEM_ID_QUERY_PARAM_KEY, itemId.toString())
                contentType(ContentType.Application.Json)
            }
        }
        val stringResponse = response.content.readUTF8Line(RESPONSE_CONTENT_READ_LIMIT) ?: ""
        return Json.decodeFromString(stringResponse)
    }

    @KtorExperimentalAPI
    suspend fun getAllItems(token: String): List<ItemData> {
        val response =  getSuccessfulResponseOrException {
            client.post() {
                url("${BASE_HTTP_URL}$GET_ALL_ITEMS_ENDPOINT")
                header(AUTHORIZATION_HEADER_NAME, token)
                contentType(ContentType.Application.Json)
            }
        }
        val stringResponse = response.content.readUTF8Line(RESPONSE_CONTENT_READ_LIMIT) ?: ""
        return Json.decodeFromString(stringResponse)
    }

    @KtorExperimentalAPI
    suspend fun getAllNotOwnedItems(token: String): List<ItemData> {
        val response =  getSuccessfulResponseOrException {
            client.post() {
                url("${BASE_HTTP_URL}$GET_NOT_OWNED_ITEMS")
                header(AUTHORIZATION_HEADER_NAME, token)
                contentType(ContentType.Application.Json)
            }
        }
        val stringResponse = response.content.readUTF8Line(RESPONSE_CONTENT_READ_LIMIT) ?: ""
        return Json.decodeFromString(stringResponse)
    }
}