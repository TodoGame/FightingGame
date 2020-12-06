package com.somegame

import com.somegame.subscription.MoneyUpdateListener
import com.somegame.subscription.UserMoneySubscriptionsManager
import com.somegame.user.User
import user.Username

class UserMoneyManager {
    companion object {
        val WINNING_USER_PRIZE = 10
        val LOSING_USER_PRIZE = 3
    }

    private val subscriptionsManager = UserMoneySubscriptionsManager()

    suspend fun subscribe(username: Username, listener: MoneyUpdateListener) {
        subscriptionsManager.subscribe(username, listener)
    }

    suspend fun unsubscribe(username: Username, listener: MoneyUpdateListener) {
        subscriptionsManager.unsubscribe(username, listener)
    }

    suspend fun unsubscribeFromAll(listener: MoneyUpdateListener) {
        subscriptionsManager.unsubscribeFromAll(listener)
    }

    suspend fun notify(username: Username, money: Int) = subscriptionsManager.notify(username, money)

    suspend fun notify(user: User) {
        notify(user.username, user.money)
    }

    suspend fun onUserWin(user: User) {
        user.acceptMoney(WINNING_USER_PRIZE)
        subscriptionsManager.notify(user.username, user.money)
    }

    suspend fun onUserLose(user: User) {
        user.acceptMoney(LOSING_USER_PRIZE)
        subscriptionsManager.notify(user.username, user.money)
    }
}
