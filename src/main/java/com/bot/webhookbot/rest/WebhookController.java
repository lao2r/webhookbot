package com.bot.webhookbot.rest;

import com.bot.webhookbot.bot.TelegramBot;
import com.bot.webhookbot.service.PayloadProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@RestController
public class WebhookController {

    private final TelegramBot telegramBot;

    private final PayloadProcessingService processingService;

    public WebhookController(TelegramBot telegramBot, PayloadProcessingService processingService) {
        this.telegramBot = telegramBot;
        this.processingService = processingService;
    }

    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBot.onWebhookUpdateReceived(update);
    }

    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody String payload) throws IOException, TelegramApiException {
        processingService.processPayload(payload);
    }

    @GetMapping("/webhook")
    public ResponseEntity<?> checkStatus() {
        return ResponseEntity.ok("Bot is active");
    }

}