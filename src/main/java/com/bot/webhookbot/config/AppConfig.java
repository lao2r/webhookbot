package com.bot.webhookbot.config;

import com.bot.webhookbot.bot.TelegramBot;
import com.bot.webhookbot.facade.TelegramFacade;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

import java.util.Properties;

@Configuration
public class AppConfig {

    private final Properties telegramBotProperties;

    public AppConfig(@Qualifier("telegramBotProperties") Properties telegramBotProperties) {
        this.telegramBotProperties = telegramBotProperties;
    }

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(telegramBotProperties.getProperty("webHookPath")).build();
    }

    @Bean
    public TelegramBot springWebhookBot(SetWebhook setWebhook, TelegramFacade telegramFacade) {
        TelegramBot bot = new TelegramBot(telegramFacade, setWebhook);
        bot.setBotToken(telegramBotProperties.getProperty("botToken"));
        bot.setBotUsername(telegramBotProperties.getProperty("userName"));
        bot.setBotPath(telegramBotProperties.getProperty("webHookPath"));
        return bot;
    }
}