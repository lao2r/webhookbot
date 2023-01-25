package com.bot.webhookbot.service;

import com.bot.webhookbot.bot.TelegramBot;
import com.bot.webhookbot.config.TelegramBotConfig;
import com.bot.webhookbot.model.GitlabMergeRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.RepositoryFile;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
            if (event.getObjectAttributes().getState() != null) {
                String projectId = String.valueOf(event.getProject().getId());
                String mergeState = event.getObjectAttributes().getState();
                String mergeStatus = event.getObjectAttributes().getMergeStatus();
                String currentVersion = getFileContent((projectId), "pom.xml");
                String createdAt = event.getObjectAttributes().getCreatedAt().replaceAll("T", " ");
                String url = event.getObjectAttributes().getUrl().replaceAll("-", "\\\\-");
                message = "New merge request #" + event.getObjectAttributes().getIid() + " " +
                        event.getObjectAttributes().getTitle() +
                        " by " + event.getObjectAttributes().getLastCommit().getAuthor().getName() + "\n" +
                        "Created at: " + createdAt.substring(0, createdAt.length() - 2).replaceAll("U", "") + "\n" +
                        "Merge status: " + mergeStatus + "\n" +
                        "Merge state: " + mergeState.toUpperCase() + "\n" +
                        "Current version: " + currentVersion;
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

                sendMessage(message, projectId);
            }
        }
    }

    public String getFileContent(String projectId, String filePath) {
        GitLabApi gitLabApi = new GitLabApi("https://gitlab.akb-it.ru/", config.getGitlabToken());

        try {
            RepositoryFile file = gitLabApi.getRepositoryFileApi().getFile(projectId, filePath, "develop");
            return processXml(file.getContent());
        } catch (GitLabApiException e) {
            e.printStackTrace();
            return "Error retrieving file.";
        }
    }

    public String processXml(String base64XmlString) {
        byte[] xmlData = Base64.getDecoder().decode(base64XmlString);
        SAXBuilder saxBuilder = new SAXBuilder();
        String xmlString = new String(xmlData, StandardCharsets.UTF_8);

        try {
            Document document = saxBuilder.build(new StringReader(xmlString));
            Element root = document.getRootElement();
            return root.getChildText("version", root.getNamespace());

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendMessage(String processedMessage, String projectId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        String chatId;

        if (projectId.equals("4102")) {
            chatId = "-1001538172303L";
        } else if (projectId.equals("3997")) {
            chatId = "-1001851993042L";
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
