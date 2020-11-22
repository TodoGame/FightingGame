package com.somegame.shop

import com.somegame.security.UserPrincipal
import com.somegame.user.service.UserService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import shop.ShopEndpoints

fun Routing.shop() {

    val userService: UserService by inject()

    authenticate {
        post(ShopEndpoints.BUY_ITEM_ENDPOINT) {
            val userPrincipal = call.principal<UserPrincipal>() ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }
            val itemId = try {
                call.receive<Int>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val user = userService.buyItem(itemId, userPrincipal.username)
            call.respond(user.getPublicData())
        }
    }
}