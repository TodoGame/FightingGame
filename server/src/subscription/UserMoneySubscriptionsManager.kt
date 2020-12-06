package com.somegame.subscription

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import user.Username

typealias MoneyUpdateListener = suspend (Username, Int) -> Unit

class UserMoneySubscriptionsManager {

    private val mutex = Mutex()

    private val listeners = mutableMapOf<Username, MutableSet<MoneyUpdateListener>>()

    suspend fun subscribe(username: Username, listener: MoneyUpdateListener) = mutex.withLock {
        val userListeners = listeners[username] ?: mutableSetOf<MoneyUpdateListener>().also {
            listeners[username] = it
        }
        userListeners.add(listener)
    }

    suspend fun unsubscribe(username: Username, listener: MoneyUpdateListener) = mutex.withLock {
        unsubscribeFromEach(username, listener)
    }

    private fun unsubscribeFromEach(username: Username, listener: MoneyUpdateListener) {
        val userListeners = listeners[username] ?: return
        userListeners.remove(listener)
        if (userListeners.size == 0) {
            listeners.remove(username)
        }
    }

    suspend fun unsubscribeFromAll(listener: MoneyUpdateListener) = mutex.withLock {
        for (username in listeners.keys) {
            unsubscribeFromEach(username, listener)
        }
    }

    suspend fun notify(username: Username, money: Int) = mutex.withLock {
        listeners[username]?.forEach { it(username, money) }
    }
}
