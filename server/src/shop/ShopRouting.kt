package com.somegame.shop

import com.somegame.handleReceiveExceptions
import com.somegame.items.ItemRepository
import com.somegame.responseExceptions.ConflictException
import com.somegame.responseExceptions.ForbiddenException
import com.somegame.responseExceptions.NotFoundException
import com.somegame.security.SecurityUtils.user
import com.somegame.user.UserExtensions
import com.somegame.user.UserExtensions.buyItem
import com.somegame.user.UserExtensions.publicData
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import shop.ShopEndpoints

fun Routing.shop() {
    val itemRepository: ItemRepository by inject()

    authenticate {
        post(ShopEndpoints.BUY_ITEM_ENDPOINT) {
            val user = call.user()
            val itemId = handleReceiveExceptions { call.receive<Int>() }
            val item = itemRepository.getItemById(itemId) ?: throw NotFoundException("Item with id=$itemId not found")
            try {
                user.buyItem(item)
                call.respond(user.publicData())
            } catch (e: UserExtensions.ItemAlreadyInInventoryException) {
                throw ConflictException(e.message)
            } catch (e: UserExtensions.NotEnoughMoneyException) {
                throw ForbiddenException(e.message)
            }
        }
    }
}
