package com.somegame.shop

import com.somegame.items.ItemRepository
import com.somegame.security.UnauthorizedException
import com.somegame.security.UserPrincipal
import com.somegame.user.User
import com.somegame.user.service.UserService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import shop.ShopEndpoints

fun Routing.shop() {

    val itemRepository: ItemRepository by inject()
    val userService: UserService by inject()

    authenticate {
        post(ShopEndpoints.BUY_ITEM_ENDPOINT) {
            val userPrincipal = call.principal<UserPrincipal>() ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }
            val user = userService.findUserByUsername(userPrincipal.username)
                ?: throw UnauthorizedException("User was not found")
            val itemId = try {
                call.receive<Int>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val item = itemRepository.getItemById(itemId) ?: throw BadRequestException("Item with id=$itemId not found")
            try {
                user.addToInventory(item)
                call.respond(user.getPublicData())
            } catch (e: User.ItemAlreadyInInventoryException) {
                call.respond(HttpStatusCode.Conflict, "Item already in inventory")
            }
        }
    }
}