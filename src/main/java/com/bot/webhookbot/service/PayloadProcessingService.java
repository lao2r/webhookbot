package com.bot.webhookbot.service;

import com.bot.webhookbot.bot.TelegramBot;
import com.bot.webhookbot.config.TelegramBotConfig;
import com.bot.webhookbot.model.GitlabMergeRequestEvent;
import com.bot.webhookbot.util.GitlabUtils;
import com.bot.webhookbot.util.MessageUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Service
public class PayloadProcessingService {

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private TelegramBotConfig config;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GitlabUtils gitlabUtils;

    @Autowired
    private MessageUtils messageUtils;

    public void processPayload(String payload) throws JsonProcessingException, TelegramApiException {
        GitlabMergeRequestEvent event = objectMapper.readValue(payload, GitlabMergeRequestEvent.class);

        if (event.getObjectKind().equals("merge_request")) {
            if (event.getObjectAttributes().getState() != null) {
                String mergeState = event.getObjectAttributes().getState();
                if (mergeState.equalsIgnoreCase(config.getMergeState())) {
                    String projectId = String.valueOf(event.getProject().getId());
                    sendMessage(messageUtils.processMesage(event, gitlabUtils, projectId, mergeState), projectId);
                }
            }
        }
    }

    public void sendMessage(String processedMessage, String projectId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        String chatId;

        if (projectId.equals(config.getProjectIdShared())) {
            chatId = config.getSharedChatId();
        } else if (projectId.equals(config.getProjectIdCommon())) {
            chatId = config.getCommonChatId();
        } else {
            chatId = config.getDefaultChatId();
        }

        sendMessage.setText(processedMessage);
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);
        sendMessage.setDisableWebPagePreview(true);
        telegramBot.execute(sendMessage);
    }
}
