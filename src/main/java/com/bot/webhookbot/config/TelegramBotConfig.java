package com.bot.webhookbot.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramBotConfig {

    @Value("${telegrambot.webHookPath}")
    String webHookPath;

    @Value("${telegrambot.userName}")
    String userName;

    @Value("${telegrambot.botToken}")
    String botToken;

    @Value("${telegrambot.defaultChatId}")
    String defaultChatId;

    @Value("${telegrambot.sharedChatId}")
    String sharedChatId;

    @Value("${telegrambot.commonChatId}")
    String commonChatId;

    @Value("${telegrambot.gitlabToken}")
    String gitlabToken;

    @Value("${telegrambot.projectId.shared}")
    String projectIdShared;

    @Value("${telegrambot.projectId.common}")
    String projectIdCommon;

    @Value("${telegrambot.mergeRequest.state}")
    String mergeState;

    @Value("${telegrambot.url.project}")
    String projectUrl;

}