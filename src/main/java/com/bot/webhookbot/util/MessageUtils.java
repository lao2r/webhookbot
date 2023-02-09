package com.bot.webhookbot.util;

import com.bot.webhookbot.model.GitlabMergeRequestEvent;
import org.springframework.stereotype.Component;

@Component
public class MessageUtils {

    public String processMesage(GitlabMergeRequestEvent event, GitlabUtils gitlabUtils, String projectId, String mergeState) {
        String modules = gitlabUtils.getAllModules(event, projectId);
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

        return message;
    }
}
