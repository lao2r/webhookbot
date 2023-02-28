package com.bot.webhookbot.util;

import com.bot.webhookbot.config.TelegramBotConfig;
import com.bot.webhookbot.model.Commit;
import com.bot.webhookbot.model.Diff;
import com.bot.webhookbot.model.GitlabMergeRequestEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.RepositoryFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GitlabUtils {

    @Autowired
    private TelegramBotConfig config;

    @Autowired
    private OkHttpClient client;

    @Autowired
    private XMLUtils xmlUtils;

    @Autowired
    private ObjectMapper objectMapper;

    public String getFileContent(String projectId, String filePath) {
        GitLabApi gitLabApi = new GitLabApi(config.getProjectUrl(), config.getGitlabToken());
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
                .url(config.getProjectUrl() + "api/v4/projects/" + projectId + "/repository/commits/" + commitHash + "/diff")
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
                .url(config.getProjectUrl() + "api/v4/projects/" + projectId + "/merge_requests/" + mergeRequestIid + "/commits")
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

    public Set<String> getAllArtifacts(GitlabMergeRequestEvent.ObjectAttributes attributes, String projectId) {
        Set<String> artifacts = new HashSet<>();

        getAllCommits(attributes, projectId).forEach(commit -> {
            try {
                Set<String> artifact = objectMapper.readValue(getCommitInfo(Integer.parseInt(projectId),
                                commit), new TypeReference<List<Diff>>() {})
                        .stream()
                        .filter(path -> !path.getNewPath().contains("pom.xml"))
                        .map(e -> e.getNewPath().replaceAll("\\/.*", ""))
                        .collect(Collectors.toSet());
                artifacts.addAll(artifact);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        return artifacts;
    }

    public String getAllModules(GitlabMergeRequestEvent.ObjectAttributes attributes, String projectId) {
        try {
            return String.join(",", getAllArtifacts(attributes, projectId)
                    .stream()
                    .map(e -> e + "(" + getFileContent((projectId), String.format("%s/pom.xml", e)) + ")")
                    .collect(Collectors.toSet()));
        } catch (Exception e) {
            e.printStackTrace();
            return "No info about any module";
        }
    }

    public List<String> getAllCommits(GitlabMergeRequestEvent.ObjectAttributes attributes, String projectId) {
        try {
            return objectMapper.readValue(getCommits(Integer.parseInt(projectId),
                            attributes.getIid()), new TypeReference<List<Commit>>() {})
                    .stream()
                    .filter(commit -> !commit.getTitle().contains("ci skip"))
                    .map(Commit::getId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
