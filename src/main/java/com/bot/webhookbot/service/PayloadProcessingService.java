package com.bot.webhookbot.service;

import com.bot.webhookbot.bot.TelegramBot;
import com.bot.webhookbot.config.TelegramBotConfig;
import com.bot.webhookbot.model.GitlabMergeRequestEvent;
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

    public void processPayload(String payload) throws JsonProcessingException, TelegramApiException {
        ObjectMapper mapper = new ObjectMapper();
        GitlabMergeRequestEvent event = mapper.readValue(payload, GitlabMergeRequestEvent.class);
        String message;

        if (event.getObjectKind().equals("merge_request")) {
            String mergeState = event.getObjectAttributes().getState();

            if (mergeState.equals("merged")) {
                String createdAt = event.getObjectAttributes().getCreatedAt().replaceAll("T", " ");
                String url = event.getObjectAttributes().getUrl().replaceAll("-", "\\\\-");
                message = "New merge request #" + event.getObjectAttributes().getIid() + " " +
                        event.getObjectAttributes().getTitle() +
                        " by " + event.getObjectAttributes().getLastCommit().getAuthor().getName() + "\n" +
                        "Created at: " + createdAt.substring(0, createdAt.length() - 2).replaceAll("U", "") + "\n" +
                        "Merge state: " + mergeState;
                message = message
                        .replaceAll("\\(", "\\\\(")
                        .replaceAll("\\)", "\\\\)")
                        .replaceAll("\\[", "\\\\[")
                        .replaceAll("]", "\\\\]")
                        .replaceAll("\\.", "\\\\.")
                        .replaceAll("!", "\\\\!")
                        .replaceAll("#", "\\\\#")
                        .replaceAll("_", "\\\\_")
                        .replaceAll("-", "\\\\-");
                message = message + "\n" +
                        "[Ссылка на MERGE REQUEST](" + url + ")";
                sendMessage(message);
            }
        }
    }

    public void sendMessage(String processedMessage) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(processedMessage);
        sendMessage.setChatId(config.getDefaultChatId());
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);
        sendMessage.setDisableWebPagePreview(true);
        telegramBot.execute(sendMessage);
    }
}
