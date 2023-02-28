package com.bot.webhookbot.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Configuration
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@PropertySource("file:application.yml")
public class TelegramBotConfig {

    @Bean("telegramBotProperties")
    public Properties getProperties() {
        File propertiesFile = new File("application.yml");
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

}