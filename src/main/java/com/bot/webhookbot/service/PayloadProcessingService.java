package com.bot.webhookbot.service;

import com.bot.webhookbot.bot.TelegramBot;
import com.bot.webhookbot.config.TelegramBotConfig;
import com.bot.webhookbot.model.Commit;
import com.bot.webhookbot.model.Diff;
import com.bot.webhookbot.model.GitlabMergeRequestEvent;
import com.bot.webhookbot.util.GitlabUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void processPayload(String payload) throws JsonProcessingException, TelegramApiException {
        GitlabMergeRequestEvent event = objectMapper.readValue(payload, GitlabMergeRequestEvent.class);
        String modules = null;

        if (event.getObjectKind().equals("merge_request")) {
            if (event.getObjectAttributes().getState() != null) {
                String mergeState = event.getObjectAttributes().getState();

                if (mergeState.equalsIgnoreCase("MERGED")) {
                    String projectId = String.valueOf(event.getProject().getId());
                    try {
                        List<String> commits = objectMapper.readValue(gitlabUtils.getCommits(event.getProject().getId(),
                                event.getObjectAttributes().getIid()), new TypeReference<List<Commit>>() {})
                                .stream()
                                .filter(commit -> !commit.getTitle().contains("ci skip"))
                                .map(Commit::getId)
                                .collect(Collectors.toList());

                        Set<String> artifacts = new HashSet<>();
                        commits.forEach(commit -> {
                            try {
                                Set<String> artifact = objectMapper.readValue(gitlabUtils.getCommitInfo(event.getProject().getId(),
                                        commit), new TypeReference<List<Diff>>() {})
                                        .stream()
                                        .map(e -> e.getNewPath().replaceAll("\\/.*", ""))
                                        .collect(Collectors.toSet());
                                artifacts.addAll(artifact);
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        });

                        modules = String.join(",", artifacts
                                .stream()
                                .filter(path -> !path.equals("pom.xml"))
                                .map(e -> e + "(" + gitlabUtils.getFileContent((projectId), String.format("%s/pom.xml", e)) + ")")
                                .collect(Collectors.toSet()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String mergeStatus = event.getObjectAttributes().getMergeStatus();
                    String currentVersion = gitlabUtils.getFileContent((projectId), "pom.xml");
                    String updatedAt = event.getObjectAttributes().getUpdatedAt().replaceAll("T", " ");
                    String url = event.getObjectAttributes().getUrl().replaceAll("-", "\\\\-");
                    String message = "\uD83D\uDCD6*__New merge request:__* #" + event.getObjectAttributes().getIid() + " " +
                            event.getObjectAttributes().getTitle().replaceAll("_", "\\\\_") +
                            " *__by__ *" + event.getObjectAttributes().getLastCommit().getAuthor().getName() + "\n" +
                            "⏰*__Updated at:__* " + updatedAt.substring(0, updatedAt.length() - 2).replaceAll("U", "") + "\n" +
                            "⚙️*__Merge status:__* " + mergeStatus.replaceAll("_", "\\\\_") + "\n" +
                            "✅*__Merge state:__* " + mergeState.toUpperCase() + "\n" +
                            "\uD83C\uDFF7*__General version:__* " + currentVersion + "\n" +
                            "\uD83D\uDDA5*__Modules:__* " + "```"+ modules + "```";
                    message = message
                            .replaceAll("\\(", "\\\\(")
                            .replaceAll("\\)", "\\\\)")
                            .replaceAll("\\[", "\\\\[")
                            .replaceAll("]", "\\\\]")
                            .replaceAll("\\.", "\\\\.")
                            .replaceAll("!", "\\\\!")
                            .replaceAll("#", "\\\\#")
                            .replaceAll("-", "\\\\-");
                    message = message + "\n" +
                            "\uD83D\uDD17[Ссылка на MERGE REQUEST](" + url + ")";

                    sendMessage(message, projectId);
                }
            }
        }
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
