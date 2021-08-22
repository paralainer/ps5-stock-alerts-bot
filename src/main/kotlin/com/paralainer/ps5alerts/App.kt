package com.paralainer.ps5alerts

import kotlinx.coroutines.flow.collect
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ChatId
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val bot = bot {
        token = System.getenv("TG_TOKEN")
    }
    val messages = TwitterFeedChecker().subscribe()
    println("started")
    messages.collect {
        println("sending message: " + it.text)
        runCatching {
            bot.sendMessage(ChatId.fromChannelUsername("ps5stockalertsau"), it.text)
        }.onFailure { err ->
            println(err)
        }
    }
}
