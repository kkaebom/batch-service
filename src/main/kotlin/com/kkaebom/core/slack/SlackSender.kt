package com.kkaebom.core.slack

import com.kkaebom.core.properties.SlackProperties
import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.SlackApiException
import com.slack.api.methods.request.chat.ChatPostMessageRequest.ChatPostMessageRequestBuilder
import com.slack.api.methods.request.conversations.ConversationsListRequest.ConversationsListRequestBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class SlackSender(val slackProperties: SlackProperties) {
    val log: Logger = LoggerFactory.getLogger(SlackSender::class.java)

    fun findChannel() {
        val appConfig = generatedAppConfig()
        val client = App(appConfig).client

        client.conversationsList { r: ConversationsListRequestBuilder ->
            r.token(slackProperties.botToken)
        }.channels.forEach { log.info("id: ${it.id}, name: ${it.name}") }
    }

    @Throws(SlackApiException::class, IOException::class)
    fun sendSlackMessage(message: String, channel: SlackChannel) {
        val appConfig = generatedAppConfig()
        val client = App(appConfig).client()
        sendMessage(message, client, channel)
    }

    protected fun generatedAppConfig(): AppConfig {
        val appConfig = AppConfig()
        appConfig.singleTeamBotToken = slackProperties.botToken
        appConfig.signingSecret = slackProperties.signingSecret
        return appConfig
    }

    @Throws(IOException::class, SlackApiException::class)
    protected fun sendMessage(message: String, client: MethodsClient, channel: SlackChannel) {
        if (message.isBlank()) return

        val channelId: String = when (channel) {
            SlackChannel.CRAWLING -> slackProperties.crawlingChannelId
        }

        client.chatPostMessage { r: ChatPostMessageRequestBuilder ->
            r.token(slackProperties.botToken)
                .channel(channelId)
                .text(message)
        }
    }

}