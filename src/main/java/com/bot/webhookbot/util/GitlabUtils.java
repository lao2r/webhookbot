package com.bot.webhookbot.util;

import com.bot.webhookbot.config.TelegramBotConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.RepositoryFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GitlabUtils {

    @Autowired
    private TelegramBotConfig config;

    @Autowired
    private OkHttpClient client;

    @Autowired
    private XMLUtils xmlUtils;

    public String getFileContent(String projectId, String filePath) {
        GitLabApi gitLabApi = new GitLabApi("https://gitlab.akb-it.ru/", config.getGitlabToken());
        RepositoryFile file;
        try {
            file = gitLabApi.getRepositoryFileApi().getFile(projectId, filePath, "develop");
            return xmlUtils.processXml(file.getContent());
        } catch (GitLabApiException e) {
            e.printStackTrace();
            return "Error retrieving file.";
        }
    }

    public String getCommitInfo(int projectId, String commitHash) throws Exception {
        Request request = new Request.Builder()
                .url("https://gitlab.akb-it.ru/api/v4/projects/" + projectId + "/repository/commits/" + commitHash + "/diff")
                .addHeader("Private-Token", config.getGitlabToken())
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Failed to get commits: " + response);
            }
            return response.peekBody(Long.MAX_VALUE).string();
        }
    }

    public String getCommits(int projectId, int mergeRequestIid) throws Exception {
        Request request = new Request.Builder()
                .url("https://gitlab.akb-it.ru/api/v4/projects/" + projectId + "/merge_requests/" + mergeRequestIid + "/commits")
                .addHeader("Private-Token", config.getGitlabToken())
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Failed to get commit information: " + response);
            }
            return response.peekBody(Long.MAX_VALUE).string();
        }
    }
}
