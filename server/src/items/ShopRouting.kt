package com.somegame.shop

import com.somegame.handleReceiveExceptions
import com.somegame.items.ItemRepository
import com.somegame.items.publicData
import com.somegame.requiredIdParameter
import com.somegame.responseExceptions.ConflictException
import com.somegame.responseExceptions.ForbiddenException
import com.somegame.responseExceptions.NotFoundException
import com.somegame.security.SecurityUtils.user
import com.somegame.user.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import shop.ShopEndpoints

fun Routing.shop() {
    val itemRepository: ItemRepository by inject()

    get(ShopEndpoints.GET_ALL_ITEMS_ENDPOINT) {
        val items = itemRepository.getAllItems().map { it.publicData() }
        call.respond(items)
    }

    get(ShopEndpoints.GET_ITEM_ENDPOINT) {
        val itemId = call.requiredIdParameter()
        val item = itemRepository.getItemById(itemId) ?: throw NotFoundException("Item with id=$itemId not found")
        call.respond(item.publicData())
    }

    authenticate {
        post(ShopEndpoints.BUY_ITEM_ENDPOINT) {
            val user = call.user()
            val itemId = handleReceiveExceptions { call.receive<Int>() }
            val item = itemRepository.getItemById(itemId) ?: throw NotFoundException("Item with id=$itemId not found")
            try {
                user.buyItem(item)
                call.respond(user.publicData())
            } catch (e: ItemAlreadyInInventoryException) {
                throw ConflictException(e.message)
            } catch (e: NotEnoughMoneyException) {
                throw ForbiddenException(e.message)
            }
        }

        get(ShopEndpoints.GET_NOT_OWNED_ITEMS) {
            val user = call.user()
            val notOwnedItems = itemRepository.getAllItems().filterNot { user.hasItem(it) }
            call.respond(notOwnedItems.map { it.publicData() })
        }
    }
}
