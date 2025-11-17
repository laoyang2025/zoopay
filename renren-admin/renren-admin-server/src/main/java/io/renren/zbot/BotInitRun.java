package io.renren.zbot;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.dao.SysDeptDao;
import io.renren.entity.SysDeptEntity;
import io.renren.zapi.ZConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@Slf4j
public class BotInitRun implements ApplicationRunner {

    @Resource
    private ZConfig config;
    @Resource
    private BotContext botContext;
    @Resource
    private SysDeptDao sysDeptDao;

    private void startBot(SysDeptEntity dept) {
        try {
            DefaultBotOptions botOptions = new DefaultBotOptions();
            if (config.isDev()) {
                log.info("in dev: set proxy for telegram");
                botOptions.setProxyHost("127.0.0.1");
                botOptions.setProxyPort(10010);
                botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
            }
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            BotService botService = new BotService(botOptions, botContext, dept.getBotKey(), dept.getBotName());
            try {
                botsApi.registerBot(botService);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } catch (TelegramApiException e) {
            log.error("start bot helper failed");
            throw new RuntimeException(e);
        }
        log.info("start bot helper success!");
    }

    @Override
    public void run(ApplicationArguments args) {
        for (SysDeptEntity deptEntity : sysDeptDao.selectList(Wrappers.emptyWrapper())) {
            if(StringUtils.isNotEmpty(deptEntity.getBotKey())) {
                startBot(deptEntity);
            }
        }
    }
}
