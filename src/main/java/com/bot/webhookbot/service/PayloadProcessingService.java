package com.bot.webhookbot.service;

import com.bot.webhookbot.bot.TelegramBot;
import com.bot.webhookbot.config.TelegramBotConfig;
import com.bot.webhookbot.model.GitlabMergeRequestEvent;
import com.bot.webhookbot.util.GitlabUtils;
import com.bot.webhookbot.util.MessageUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Properties;


@Service
public class PayloadProcessingService {

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private TelegramBotConfig config;

    @Autowired
    @Qualifier("telegramBotProperties")
    private Properties properties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageUtils messageUtils;

    public void processPayload(String payload) throws JsonProcessingException, TelegramApiException {
        GitlabMergeRequestEvent event = objectMapper.readValue(payload, GitlabMergeRequestEvent.class);

        if (event.getObjectKind().equals("merge_request")) {
            if (event.getObjectAttributes().getState() != null) {
                String mergeState = event.getObjectAttributes().getState();
                if (mergeState.equalsIgnoreCase(config.getProperties().getProperty("state"))) {
                    String projectId = String.valueOf(event.getProject().getId());
                    sendMessage(messageUtils.processMessage(event.getObjectAttributes(), projectId, mergeState), projectId);
                }
            }
        }
    }

    public void sendMessage(String processedMessage, String projectId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        String chatId;

        if (projectId.equals(properties.getProperty("sharedChatId"))) {
            chatId = properties.getProperty("sharedChatId");
        } else if (projectId.equals(properties.getProperty("commonChatId"))) {
            chatId = properties.getProperty("commonChatId");
        } else {
            chatId = properties.getProperty("defaultChatId");
        }

        sendMessage.setText(processedMessage);
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);
        sendMessage.setDisableWebPagePreview(true);
        telegramBot.execute(sendMessage);
    }
}
