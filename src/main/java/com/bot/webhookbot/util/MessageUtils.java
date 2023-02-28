package com.bot.webhookbot.util;

import com.bot.webhookbot.model.GitlabMergeRequestEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageUtils {

    @Autowired
    private GitlabUtils gitlabUtils;

    public String processMessage(GitlabMergeRequestEvent.ObjectAttributes attributes, String projectId, String mergeState) {
        String message =
                formatMergeRequest(attributes) +
                formatTitle(attributes) +
                formatAuthor(attributes) +
                formatUpdatedAt(attributes) +
                formatMergeStatus(attributes) +
                formatMergeState(mergeState) +
                formatGeneralVersion((projectId), "pom.xml") +
                formatModules(attributes, projectId) +
                formatDescription(attributes);
        return formatMessage(message) + formatUrl(attributes);
    }

    private String formatMergeRequest(GitlabMergeRequestEvent.ObjectAttributes attributes) {
        return "\uD83D\uDCD6*__New merge request:__* #" + attributes.getIid() + " ";
    }

    private String formatAuthor(GitlabMergeRequestEvent.ObjectAttributes attributes) {
        return " *__by__ *" + attributes.getLastCommit().getAuthor().getName() + "\n";
    }

    private String formatMergeState(String mergeState) {
        return "✅*__Merge state:__* " + mergeState.toUpperCase() + "\n";
    }

    private String formatGeneralVersion(String projectId, String filePath) {
        return "\uD83C\uDFF7*__General version:__* " + gitlabUtils.getFileContent((projectId), filePath) + "\n";
    }

    private String formatModules(GitlabMergeRequestEvent.ObjectAttributes attributes, String projectId) {
        return "\uD83D\uDDA5*__Modules:__* " + "```"+ gitlabUtils.getAllModules(attributes, projectId) + "```" + "\n";
    }

    private String formatUpdatedAt(GitlabMergeRequestEvent.ObjectAttributes attributes) {
        return "⏰*__Updated at:__* " + attributes.getUpdatedAt().replaceAll("T", " ")
                .substring(0, attributes.getUpdatedAt().length() - 2)
                .replaceAll("U", "") + "\n";
    }

    private String formatMergeStatus(GitlabMergeRequestEvent.ObjectAttributes attributes) {
        return "⚙️*__Merge status:__* " + attributes.getMergeStatus().replaceAll("_", "\\\\_") + "\n";
    }

    private String formatTitle(GitlabMergeRequestEvent.ObjectAttributes attributes) {
        return attributes.getTitle().replaceAll("_", "\\\\_");
    }

    private String formatDescription(GitlabMergeRequestEvent.ObjectAttributes attributes) {
        return "⚠️*__Description:__* " + ((attributes.getDescription() == null ||
                                             attributes.getDescription().isEmpty()) ? "No description" :
                                             attributes.getDescription().replaceAll("_", "\\\\_") + "\n");
    }

    private String formatUrl(GitlabMergeRequestEvent.ObjectAttributes attributes) {
        return "\n" + "\uD83D\uDD17[Ссылка на MERGE REQUEST](" + attributes.getUrl().replaceAll("-", "\\\\-") + ")";
    }

    private String formatMessage(String message) {
        return message
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)")
                .replaceAll("\\[", "\\\\[")
                .replaceAll("]", "\\\\]")
                .replaceAll("\\.", "\\\\.")
                .replaceAll("!", "\\\\!")
                .replaceAll("#", "\\\\#")
                .replaceAll("-", "\\\\-");
    }
}
