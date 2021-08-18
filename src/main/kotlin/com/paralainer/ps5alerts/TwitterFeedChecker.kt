package com.paralainer.ps5alerts

import io.github.redouane59.twitter.TwitterClient
import io.github.redouane59.twitter.dto.endpoints.AdditionalParameters
import io.github.redouane59.twitter.dto.tweet.TweetType
import io.github.redouane59.twitter.signature.TwitterCredentials
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.*

class TwitterFeedChecker(
    private val delay: Duration = Duration.ofSeconds(20)
) {

    private val twitterClient = TwitterClient(
        TwitterCredentials.builder()
            .apiKey(System.getenv("TW_API_KEY"))
            .apiSecretKey(System.getenv("TW_API_SECRET"))
            .accessToken(System.getenv("TW_ACCESS_TOKEN"))
            .accessTokenSecret(System.getenv("TW_ACCESS_SECRET"))
            .build()
    )

    suspend fun subscribe(): Flow<Message> =
        flow {
            var lastUpdated: Instant = Instant.now()
            while (currentCoroutineContext().isActive) {
                println("Checking feed since: $lastUpdated")
                val latestFeed = getLatestFeed(lastUpdated)
                latestFeed.forEach {
                    if (it.createdAt.isAfter(lastUpdated)) {
                        emit(Message(it.text))
                    }
                }

                lastUpdated = latestFeed.maxOfOrNull { it.createdAt } ?: Instant.now()

                delay(delay.toMillis())
            }
        }

    private suspend fun getLatestFeed(since: Instant): List<FeedMessage> =
        withContext(Dispatchers.IO) {
            val result = twitterClient.getUserTimeline(
                "1339367546135298049",
                AdditionalParameters
                    .builder()
                    .maxResults(10)
                    .startTime(LocalDateTime.ofInstant(
                        since.minusSeconds(10), ZoneId.of("UTC")
                    ))
                    .recursiveCall(false)
                    .build()
            )

            val data = result.data.orEmpty()
            println("Fetched ${data.size} tweets")

            val filtered = data.filter {
                it.tweetType == TweetType.DEFAULT
            }
            println("${filtered.size} tweets are DEFAULT")

            filtered.map {
                FeedMessage(it.text, it.createdAt.toInstant(ZoneOffset.UTC))
            }
        }

    data class FeedMessage(val text: String, val createdAt: Instant)
}
