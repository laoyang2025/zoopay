package io.renren.bledger.service;

import io.renren.bledger.config.BledgerConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class BledgerInitRun implements ApplicationRunner {
    @Resource
    private BledgerConfig bledgerConfig;
    @Resource
    private BledgerContext bledgerContext;

    private void startBledger() {
        try {
            DefaultBotOptions botOptions = new DefaultBotOptions();

            if (bledgerConfig.isDev()) {
                log.info("in dev: set proxy for telegram");
                botOptions.setProxyHost("127.0.0.1");
                botOptions.setProxyPort(10010);
                botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
            }

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            BledgerService bledgerService = new BledgerService(botOptions, bledgerContext);
            try {
                botsApi.registerBot(bledgerService);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } catch (TelegramApiException e) {
            log.error("start bledger failed");
            throw new RuntimeException(e);
        }
        log.info("start bledger success!");
    }

    @Override
    public void run(ApplicationArguments args) {
        if (bledgerConfig.getBotKey() != null) {
            CompletableFuture.runAsync(() -> {
                startBledger();
            });
        }
    }
}
