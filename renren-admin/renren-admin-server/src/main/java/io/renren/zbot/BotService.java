package io.renren.zbot;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.tools.exception.RenException;
import io.renren.dao.SysUserDao;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dao.ZBotDao;
import io.renren.zadmin.dao.ZCardLogDao;
import io.renren.zadmin.dao.ZChannelDao;
import io.renren.zadmin.entity.*;
import io.renren.zapi.ZooConstant;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class BotService extends TelegramLongPollingBot {
    private BotContext botContext;
    private String botName;
    private String botKey;

    public BotService(DefaultBotOptions botOptions, BotContext context, String botKey, String botName) {
        super(botOptions, botKey);
        this.botContext = context;
        this.botKey = botKey;
        this.botName = botName;
    }

    @Override
    public String getBotUsername() {
        return this.botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() == null) {
            return;
        }

        if (update.getMessage().hasPhoto()) {
            Message message = update.getMessage();
            Chat chat = message.getChat();
            String botChat = chat.getId().toString();  // 所在群id
            String botText = message.getCaption();

            ZBotEntity merchant = botContext.getBotDao().selectOne(Wrappers.<ZBotEntity>lambdaQuery()
                    .eq(ZBotEntity::getChatId, chat.getId().toString())
                    .eq(ZBotEntity::getServeType, "merchant")
            );

            if (merchant == null) {
                sendTextMessage(botChat, "尚未绑定商户");
                return;
            }

            //
            if (!botText.startsWith("q ")) {
                log.error("can only process q xxxxxx");
                return;
            }

            List<PhotoSize> photos = update.getMessage().getPhoto();
            String fileId = photos.stream().sorted((ps1, ps2) -> Integer.compare(ps2.getFileSize(), ps1.getFileSize()))
                    .findFirst()
                    .orElse(null)
                    .getFileId();
            log.info("get command:{}", String.format("chat:[%s], command[%s], fileId[%s]", botChat, botText, fileId));
            String savedFile = downloadPhoto(fileId);

            // 处理商户查单请求
            handleCommand(merchant, botChat, botText, savedFile);
            return;
        }

        // 绑定商户 || 绑定渠道命令
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Chat chat = message.getChat();
            String botChat = chat.getId().toString();  // 所在群id
            String botText = message.getText();
            log.info("get command:{}", String.format("chat:[%s], command[%s]", botChat, botText));

            // help
            if (botText.startsWith("/help")) {
                sendTextMessage(botChat, "绑定商户:  bindm 机构号  商户号\n" +
                        "绑定渠道: bindc 机构好 渠道卡ID\n" +
                        "query: q xxxxxxx");
                return;
            }

            // query balance
            if (botText.startsWith("/b")) {
                merchantBalance(botChat, botText);
                return;
            }

            // bind merchant
            if (botText.startsWith("bindm ")) {
                bindMerchant(botChat, botText);
                return;
            }

            // bind channel
            if (botText.startsWith("bindc ")) {
                bindChannel(botChat, botText);
                return;
            }
        }
    }

    private void merchantBalance(String botChat, String botText) {
        ZBotEntity merchant = botContext.getBotDao().selectOne(Wrappers.<ZBotEntity>lambdaQuery()
                .eq(ZBotEntity::getChatId, botChat)
                .eq(ZBotEntity::getServeType, "merchant")
        );
        if (merchant == null) {
            sendTextMessage(botChat, "商户号未绑定");
            return;
        }

        ZBalanceEntity balanceEntity = botContext.getBalanceDao().selectOne(Wrappers.<ZBalanceEntity>lambdaQuery()
                .eq(ZBalanceEntity::getId, merchant.getServeId())
        );

        sendTextMessage(botChat, "balance = "  + balanceEntity.getBalance());
    }


    // /bind_merchant@xxxxx deptId  merchant_no
    private void bindMerchant(String chatId, String botText) {
        String[] split = botText.split("\\s+");
        long deptId = Long.parseLong(split[1]);
        String merchantNo = split[2];
        ZBotDao botDao = botContext.getBotDao();
        SysUserDao sysUserDao = botContext.getSysUserDao();
        SysUserEntity merchant = sysUserDao.selectOne(Wrappers.<SysUserEntity>lambdaQuery()
                .eq(SysUserEntity::getDeptId, deptId)
                .eq(SysUserEntity::getId, merchantNo)
        );
        ZBotEntity entity = new ZBotEntity();
        entity.setChatId(chatId);
        entity.setDeptId(deptId);
        entity.setServeId(merchant.getId());
        entity.setServeName(merchant.getUsername());
        entity.setServeType("merchant");
        botDao.insert(entity);
        sendTextMessage(chatId, "bind success");
    }

    // bind_channel@xxx deptId  card_id
    private void bindChannel(String chatId, String botText) {
        String[] split = botText.split("\\s+");
        long deptId = Long.parseLong(split[1]);
        long cardId = Long.parseLong(split[2]);
        ZBotDao botDao = botContext.getBotDao();
        ZChannelDao channelDao = botContext.getChannelDao();
        ZChannelEntity channelEntity = channelDao.selectOne(Wrappers.<ZChannelEntity>lambdaQuery()
                .eq(ZChannelEntity::getId, cardId)
                .eq(ZChannelEntity::getDeptId, deptId)
        );
        ZBotEntity entity = new ZBotEntity();
        entity.setChatId(chatId);
        entity.setDeptId(deptId);
        entity.setServeId(cardId);
        entity.setServeName(channelEntity.getChannelLabel());
        entity.setServeType("channel");
        botDao.insert(entity);
        sendTextMessage(chatId, "bind success");
    }

    private void handleCard(String chatId, String savedFile, ZChargeEntity chargeEntity) {
        String[] text = botContext.getUmiOcrService().getText(savedFile);
        for (int i = 0; i < text.length; i++) {
            String amount = fetchAmount(text[i]);
            if (amount == null) {
                sendTextMessage(chatId, "无法识别金额, 请联系客服处理 (Please contact customer service to deal with) @LQPAY07");
                return;
            }
            String utr = fetchUTR(text[i]);
            if(utr == null) {
                sendTextMessage(chatId, "无法识别UTR, 请联系客服处理 (Please contact customer service to deal with) @LQPAY07");
                return;
            }
            BigDecimal la = new BigDecimal(amount);
            ZCardLogDao cardLogDao = botContext.getCardLogDao();
            ZCardLogEntity logEntity;
            try {
                logEntity = cardLogDao.selectOne(Wrappers.<ZCardLogEntity>lambdaQuery()
                        .eq(ZCardLogEntity::getUtr, utr)
                        .eq(ZCardLogEntity::getAmount, la)
                );
            } catch (Exception ex) {
                sendTextMessage(chatId, "暂时未收到 (not received yet), utr=" + utr +",amount=" + amount);
                return;
            }

            if(logEntity == null) {
                sendTextMessage(chatId, "暂时未收到 (not received yet), utr=" + utr +",amount=" + amount);
                return;
            }

            if (logEntity.getChargeId() != null) {
                sendTextMessage(chatId, "成功（success), UTR=" + utr);
                return;
            }

            //
            if(!logEntity.getCardId().equals(chargeEntity.getCardId())) {
                sendTextMessage(chatId, "订单号错误 (Wrong order number), UTR=" + utr);
                return;
            }

            // todo:
            botContext.getCardMatchService().matchCollect(logEntity);
        }
    }

    private  void handleChannel(String chatId, String savedFile, ZChargeEntity chargeEntity) {
        Long cardId = chargeEntity.getCardId();
        ZBotEntity zBotEntity = botContext.getBotDao().selectOne(Wrappers.<ZBotEntity>lambdaQuery()
                .eq(ZBotEntity::getServeType, "channel")
                .eq(ZBotEntity::getServeId, cardId)
        );
        if(zBotEntity == null) {
            sendTextMessage(chatId, "尚未绑定渠道:" + chargeEntity.getCardUser());
            return;
        }
        try {
            sendPhotoMessage(zBotEntity.getChatId(), "查单:" + chargeEntity.getId().toString(), savedFile);
            sendTextMessage(chatId, "转发到渠道成功:" + chargeEntity.getCardUser());
        } catch (TelegramApiException e) {
            sendTextMessage(chatId, "转发到渠道失败:" + chargeEntity.getCardUser());
        }
    }

    private void handleCommand(ZBotEntity merchant, String chatId, String botText, String savedFile) {
        String[] split = botText.split("\\s+");
        String orderId = split[1];
        ZChargeEntity chargeEntity = botContext.getChargeDao().selectOne(
                Wrappers.<ZChargeEntity>lambdaQuery()
                        .eq(ZChargeEntity::getMerchantId, merchant.getServeId())
                        .eq(ZChargeEntity::getOrderId, orderId)
        );
        if (chargeEntity == null) {
            sendTextMessage(chatId, "订单号不是我们的 (The order number is not ours), orderId=" + orderId);
            return;
        }

        if(chargeEntity.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_SUCCESS)) {
            sendTextMessage(chatId, "成功（success), orderId=" + orderId);
            return;
        }

        // 自运营卡匹配
        if(chargeEntity.getHandleMode().equals(ZooConstant.PROCESS_MODE_CHANNEL)) {
            handleCard(chatId, savedFile, chargeEntity);
            return;
        }

        // 渠道匹配
        handleChannel(chatId, savedFile, chargeEntity);
    }

    // 数字匹配
    private static List<Pattern> moneyPatterns = List.of(
            Pattern.compile("PaidSuccessfully\\s+(\\d+)"),
            Pattern.compile("心\\s+(\\d+)\s+心"),
            Pattern.compile("Amount\s+(\\d+)"),
            Pattern.compile("(\\d+)\s+\\d+\s+心"),
            Pattern.compile("> 心买(\\d+)"),
            Pattern.compile("\\d+\\s+心\\s+(\\d+)\s+心"),
            Pattern.compile("(\\d+\\.\\d{2})")
    );
    private static List<Pattern> utrPatterns = List.of(
            Pattern.compile("UTR:(\\d+)"),
            Pattern.compile("UPIRefNo:(\\d+)Copy"),
            Pattern.compile("UPIRefNo:(\\d+)\s+.*"),
            Pattern.compile("(?:X|x){3,}\\d+\s+UTR:(\\d+)"),
            Pattern.compile("X{3,}\\d+\s+UTR：(\\d+)")
    );

    private String fetchUTR(String text) {
        for (Pattern utrPattern : utrPatterns) {
            Matcher matcher = utrPattern.matcher(text);
            if(matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private String fetchAmount(String text) {
        for (Pattern moneyPattern : moneyPatterns) {
            Matcher matcher = moneyPattern.matcher(text);
            if(matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public void sendTextMessage(String chatId, String text) {
        try {
            execute(new SendMessage(chatId, text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPhotoMessage(String chatId, String caption, String savedFile) throws TelegramApiException {
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(chatId);  // 替换成您的目标群组的 chatId
        sendPhotoRequest.setCaption(caption);
        sendPhotoRequest.setPhoto(new InputFile(savedFile));
        execute(sendPhotoRequest);
    }

    private String downloadPhoto(String fileId) {
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(fileId);
        String savedFile = null;
        try {
            File file = execute(getFileMethod);
            String fileUrl = "https://api.telegram.org/file/bot" + this.botKey + "/" + file.getFilePath();
            log.info("download fileUrl:{}", fileUrl);
            URL url = new URL(fileUrl);
            InputStream in = new BufferedInputStream(url.openStream());

            java.io.File dir =new java.io.File("/tmp/photos");
            dir.mkdirs();

            savedFile = "/tmp/" + file.getFilePath();
            OutputStream out = new FileOutputStream(savedFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            in.close();
            out.close();
            log.info("download photo to {}", savedFile);
            return savedFile;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("File download failed.");
        }
        log.error("can not download photo");
        throw new RenException("can not download photo");
    }

}