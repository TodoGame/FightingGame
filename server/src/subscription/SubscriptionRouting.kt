package com.somegame.subscription

import com.somegame.UserMoneyManager
import com.somegame.websocket.WebSocketService
import com.somegame.websocket.WebSocketTicketManager
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory
import subscription.*
import user.Username

val SUBSCRIPTIONS_WEBSOCKET_NAME = "sub"

fun Routing.subscription() {
    val logger = LoggerFactory.getLogger("subscriptions websocket")
    val webSocketService = WebSocketService(SUBSCRIPTIONS_WEBSOCKET_NAME)

    val helper = SubscriptionRoutingHelper()

    webSocket(SUBSCRIPTION_WEBSOCKET_ENDPOINT) {
        logger.info("New connection")
        val webSocketClient = try {
            webSocketService.tryConnect(this)
        } catch (e: WebSocketTicketManager.InvalidTicketException) {
            logger.info("Ticket not authorized")
            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid ticket"))
            return@webSocket
        }

        val client = helper.SubscriptionClient(webSocketClient)

        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val message = try {
                        Json.decodeFromString<SubscriptionMessage>(text)
                    } catch (e: SerializationException) {
                        logger.info("Could not deserialize message: $e")
                        continue
                    }
                    client.handleSubscriptionMessage(message)
                }
            }
        } finally {
            client.handleDisconnect()
        }
    }
    webSocketService.getTicketRoute(this, SUBSCRIPTION_WEBSOCKET_TICKET_ENDPOINT)
}

class SubscriptionRoutingHelper : KoinComponent {
    val logger = LoggerFactory.getLogger(javaClass)

    private val moneyPointsManager: UserMoneyManager by inject()

    inner class SubscriptionClient(private val client: WebSocketService.Client) {
        private suspend fun WebSocketService.Client.sendSubscriptionEvent(event: SubscriptionEvent) {
            sendText(Json.encodeToString(event))
        }

        private suspend fun onUserMoneyUpdate(username: Username, money: Int) {
            client.sendSubscriptionEvent(UserMoneyUpdate(username, money))
        }

        suspend fun handleSubscriptionMessage(message: SubscriptionMessage) {
            when (message) {
                is UserMoneyUpdateSubscription -> {
                    val username = message.username
                    if (message.subscribe) {
                        logger.info("Subscribing $client to User(username=$username) money updates")
                        moneyPointsManager.subscribe(username, this::onUserMoneyUpdate)
                    } else {
                        logger.info("Unsubscribing $client to User(username=$username) money updates")
                        moneyPointsManager.unsubscribe(username, this::onUserMoneyUpdate)
                    }
                }
            }
        }

        suspend fun handleDisconnect() {
            moneyPointsManager.unsubscribeFromAll(this::onUserMoneyUpdate)
            client.handleDisconnect()
        }
    }
}
