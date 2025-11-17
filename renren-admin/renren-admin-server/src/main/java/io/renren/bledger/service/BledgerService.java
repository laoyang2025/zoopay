package io.renren.bledger.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.bledger.config.BledgerConfig;
import io.renren.bledger.config.BledgerConstant;
import io.renren.bledger.dao.BotAccountDao;
import io.renren.bledger.dao.BotChargeDao;
import io.renren.bledger.dao.BotLogDao;
import io.renren.bledger.dao.BotPayDao;
import io.renren.bledger.entity.BotAccountEntity;
import io.renren.bledger.entity.BotChargeEntity;
import io.renren.bledger.entity.BotLogEntity;
import io.renren.bledger.entity.BotPayEntity;
import io.renren.dao.SysDeptDao;
import io.renren.dao.SysUserDao;
import io.renren.entity.SysDeptEntity;
import io.renren.entity.SysUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Slf4j
public class BledgerService extends TelegramLongPollingBot {
    private BledgerContext botContext;
    private BledgerConfig botmanConfig;
    private BotPayDao botPayDao;
    private BotAccountDao botAccountDao;
    private BotLogDao botLogDao;
    private BotChargeDao botChargeDao;
    private SysUserDao orgUserDao;
    private SysDeptDao orgDeptDao;
    private TransactionTemplate transactionTemplate;

    public BledgerService(DefaultBotOptions botOptions, BledgerContext context) {
        super(botOptions, context.getBotmanConfig().getBotKey());
        this.botContext = context;
        this.botmanConfig = context.getBotmanConfig();
        this.botPayDao = context.getBotPayDao();
        this.botAccountDao = context.getBotAccountDao();
        this.botLogDao = context.getBotLogDao();
        this.botChargeDao = context.getBotChargeDao();
        this.orgUserDao = context.getOrgUserDao();
        this.orgDeptDao = context.getOrgDeptDao();
        this.transactionTemplate = context.getTransactionTemplate();
    }

    @Override
    public String getBotUsername() {
        return this.botContext.getBotmanConfig().getBotName();
    }

    private void handleCommand(String botText,
                               BotAccountEntity botAccount,
                               String botChat,
                               String botAdmin,
                               Message message
    ) throws ParseException {

        // init can not be reset
        if (botText.equals("/init")) {
            sendTextMessage(message.getChatId(), "不能重复设置管理员， 请联系管理员");
            return;
        }
        if (botText.startsWith("/help")) {
            help(message);
            return;
        }
        if (botText.startsWith("check")) {
            sendTextMessage(message.getChatId(), commandCheck(botAccount, botText));
            return;
        }
        if (botText.equals("info")) {
            sendTextMessage(message.getChatId(), commandInfo(botAccount));
            return;
        }

        // admin operation
        String response = null;
        if (botAccount.getBotAdmin().equals(botAdmin)) {
            if (botText.startsWith("urate ")) {
                response = commandUsdtRate(botAccount, botText);
                sendTextMessage(message.getChatId(), response);
                return;
            }

            if (botText.startsWith("frate ")) {
                response = commandFeeRate(botAccount, botText);
                sendTextMessage(message.getChatId(), response);
                return;
            }

            // admin but usdt_rate not set
            if (botAccount.getUsdRate() == null) {
                sendTextMessage(message.getChatId(), "尚未设置USDT汇率，禁止操作");
                return;
            }
            // admin but fee_rate not set
            if (botAccount.getFeeRate() == null) {
                sendTextMessage(message.getChatId(), "尚未设置手续费率，禁止操作");
                return;
            }
            //
            if (botText.startsWith("+")) {
                sendTextMessage(message.getChatId(), commandCharge(botAccount, botText));
                return;
            }
            if (botText.startsWith("-")) {
                sendTextMessage(message.getChatId(), commandPayout(botAccount, botText));
                return;
            }
            log.info("unknown command: {}", botText);
            return;
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();

            User from = message.getFrom();
            Chat chat = message.getChat();
            String botChat = chat.getId().toString();  // 所在群id
            String botAdmin = from.getId().toString(); // 管理员id(也及时发起人id)
            String botText = message.getText();
            log.info("get command:{}", String.format("chat:[%s], user:[%s], command[%s]", botChat, botAdmin, botText));

            if (botText.startsWith("/help")) {
                help(message);
                return;
            }

            // 发起命令的人
            BotAccountEntity botAccount = botAccountDao.selectOne(Wrappers.<BotAccountEntity>lambdaQuery()
                    .eq(BotAccountEntity::getBotChat, botChat)
            );

            if (botAccount == null) {
                if (botText.startsWith("/init")) {
                    sendTextMessage(message.getChatId(), commandInit(botChat, botAdmin));
                } else {
                    sendTextMessage(message.getChatId(), "尚未设置管理员, 只能执行/init /help指令");
                }
                return;
            }

            if (botText.equals("info")) {
                sendTextMessage(message.getChatId(), commandInfo(botAccount));
                return;
            }

            if (botText.equals("check")) {
                sendTextMessage(message.getChatId(), commandCheck(botAccount, botText));
                return;
            }

            // 处理命令
            try {
                handleCommand(botText, botAccount, botChat, botAdmin, message);
            } catch (Exception ex) {
                ex.printStackTrace();
                this.help(message);
            }
        }
    }

    private void help(Message message) {
        String info =
                "/help - 展示使用手册\n" +
                        "/init - 设置管理员, 第一个执行/init命令的人将成为管理员, 管理不能更改\n" +
                        "frate - 设置手续费费率\n" +
                        "urate - 设置USDT汇率\n" +
                        "info - 查看当前余额， 手续费扣率\n" +
                        "check - 查看当日账单\n" +
                        "check 2024-06-01 - 查看2024年6月1号账单\n" +
                        "+2000 - 充值2000\n" +
                        "+2000u - 充值2000u\n" +
                        "-2000 - 下发2000\n" +
                        "-2000u - 下发2000u\n";
        sendTextMessage(message.getChatId(), info);
    }

    // 添加账号
    private String addAccount(String botChat, String botAdmin) {
        Long deptId = botmanConfig.getDeptId();
        SysDeptEntity botDeptEntity = orgDeptDao.selectOne(Wrappers.<SysDeptEntity>lambdaQuery()
                .eq(SysDeptEntity::getName, "飞机记账")
                .eq(SysDeptEntity::getPid, deptId)
        );
        if (botDeptEntity == null) {
            return "内部错误, 联系管理员";
        }
        String username = botAdmin + "#" + botChat;
        SysUserEntity userEntity = new SysUserEntity();
        userEntity.setDeptId(botDeptEntity.getId());
        userEntity.setDeptName(botDeptEntity.getName());
        userEntity.setGender(1);
        userEntity.setUsername(username);
        userEntity.setRealName(username);
        userEntity.setEmail(username + "@qq.com");

        BotAccountEntity botEntity = new BotAccountEntity();
        botEntity.setBotAdmin(botAdmin);
        botEntity.setBotChat(botChat);
        botEntity.setDeptId(userEntity.getDeptId());
        botEntity.setUserName(userEntity.getUsername());
        botEntity.setBalance(0L);
        return transactionTemplate.execute(new TransactionCallback<String>() {
            @Override
            public String doInTransaction(TransactionStatus status) {
                orgUserDao.insert(userEntity);
                botEntity.setUserId(userEntity.getId());
                botAccountDao.insert(botEntity);
                return "初始化成功, 你现在是管理员, 请设USDT汇率, 以及手续费费率";
            }
        });
    }

    /**
     * 初始化， 添加账号
     *
     * @param botChat
     * @param botAdmin
     * @return
     */
    public String commandInit(String botChat, String botAdmin) {
        BotAccountEntity botAccount = botAccountDao.selectOne(Wrappers.<BotAccountEntity>lambdaQuery()
                .eq(BotAccountEntity::getBotChat, botChat)
        );
        if (botAccount == null) {
            return addAccount(botChat, botAdmin);
        }
        return "already initialized, please do not repeat";
    }

    /**
     * 当前余额， usd汇率， 手续费率
     */
    public String commandInfo(BotAccountEntity botAccount) {
        return String.format("当前余额 - %s | %.2fu\nUSDT汇率 - %s\n 手续费率 - %s\n",
                botAccount.getBalance() / 100.0,
                botAccount.getBalance() / 100.0 / botAccount.getUsdRate().doubleValue(),
                botAccount.getUsdRate(),
                botAccount.getFeeRate()
        );
    }

    /**
     * 查看账单
     * check
     * check 2024-11-11
     * +1000
     * +1000u
     * -1000
     * -1000u
     *
     * @param botAccount
     * @param botText
     * @return
     */
    public String commandCheck(BotAccountEntity botAccount, String botText) {
        Date startOfTheDay;
        Date endOfTheDay;
        String ledgeDate;

        String[] parts = botText.split("\s+");
        if (parts.length == 1) {
            ledgeDate = DateUtil.formatDate(new Date());
        } else {
            ledgeDate = parts[1];
        }
        startOfTheDay = DateUtil.parse(ledgeDate + " 00:00:00");
        endOfTheDay = DateUtil.parse(ledgeDate + " 23:59:59");

        // 当日充值记录
        List<BotChargeEntity> charges = botChargeDao.selectList(Wrappers.<BotChargeEntity>lambdaQuery()
                .eq(BotChargeEntity::getUserId, botAccount.getUserId())
                .le(BotChargeEntity::getCreateDate, endOfTheDay)
                .ge(BotChargeEntity::getCreateDate, startOfTheDay)
        );

        // 当日付款记录
        List<BotPayEntity> botPayEntities = botPayDao.selectList(Wrappers.<BotPayEntity>lambdaQuery()
                .eq(BotPayEntity::getUserId, botAccount.getUserId())
                .le(BotPayEntity::getCreateDate, endOfTheDay)
                .ge(BotPayEntity::getCreateDate, startOfTheDay)
        );
        StringBuilder sb = new StringBuilder();
        if (charges.size() == 0) {
            sb.append(ledgeDate + "没有充值流水\n");
        } else {
            sb.append(ledgeDate + "充值流水\n");
            sb.append(
                    String.format("%s|%s|%s|%s\n", "时间", "充值金额", "usdt", "手续费")
            );
            sb.append("--------------------\n");
            long totalCharge = 0;
            int cnt = 0;
            for (BotChargeEntity charge : charges) {
                double amount = charge.getAmount() / 100.0;
                double fee = charge.getFee() / 100.0;
                sb.append(String.format("%s|%.2f|%.2f|%.2f\n",
                        DateUtil.formatTime(charge.getCreateDate()),
                        amount,
                        amount / botAccount.getUsdRate().doubleValue(),
                        fee)
                );
                totalCharge += charge.getAmount();
                cnt += 1;
            }
            sb.append("--------------------\n");
            double fTotalCharge = totalCharge / 100.0;
            sb.append(String.format("%d(笔)， 总充值: %-12.2f | %.2fu\n", cnt, fTotalCharge, fTotalCharge / botAccount.getUsdRate().doubleValue()));
        }
        sb.append("\n");

        // 下发流水
        if (botPayEntities.size() == 0) {
            sb.append(ledgeDate + "没有下发流水\n");
        } else {
            sb.append(ledgeDate + "下发流水\n");
            sb.append("--------------------\n");
            sb.append(String.format("%s|下发金额|usdt\n", "时间"));
            int cnt = 0;
            long totalWithdraw = 0L;
            for (BotPayEntity payout : botPayEntities) {
                double amount = payout.getAmount() / 100.0;
                double uAmount = payout.getAmount() / botAccount.getUsdRate().doubleValue() / 100;
                sb.append(String.format("%s|%.2f|%.2f\n", DateUtil.formatTime(payout.getCreateDate()), amount, uAmount));
                cnt += 1;
                totalWithdraw += payout.getAmount();
            }
            sb.append("--------------------\n");
            double fTotalWithdraw = totalWithdraw / 100.0;
            sb.append(String.format("%d(笔)，合计: %-10.2f | %.2fu\n", cnt, fTotalWithdraw, fTotalWithdraw / botAccount.getUsdRate().doubleValue()));
            sb.append("--------------------\n");
        }
        sb.append("\n");

        BotLogEntity logEntity = botLogDao.selectOne(Wrappers.<BotLogEntity>lambdaQuery()
                .eq(BotLogEntity::getUserId, botAccount.getUserId())
                .eq(BotLogEntity::getDeptId, botAccount.getDeptId())
                .orderByDesc(BotLogEntity::getCreateDate)
                .last("limit 1")
        );
        if (logEntity == null) {
            sb.append(ledgeDate + "没有 充值&下发\n");
        } else {
            double fBalance = logEntity.getNewAmount().longValue() / 100.0;
            sb.append(String.format(ledgeDate + "日末余额:  %-10.2f | %.2fu", fBalance, fBalance / botAccount.getUsdRate().doubleValue()));
        }
        return sb.toString();
    }

    public String commandUsdtRate(BotAccountEntity botAccount, String usdtRate) {
        usdtRate = usdtRate.split("\s+")[1];
        botAccountDao.update(null, Wrappers.<BotAccountEntity>lambdaUpdate()
                .eq(BotAccountEntity::getId, botAccount.getId())
                .set(BotAccountEntity::getUsdRate, new BigDecimal(usdtRate))
        );
        return "USDT汇率设置成功";
    }

    /**
     * 设置扣率
     *
     * @param botAccount
     * @param feeRate
     * @return
     * @throws ParseException
     */
    public String commandFeeRate(BotAccountEntity botAccount, String feeRate) throws ParseException {
        feeRate = feeRate.split("\s+")[1];

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setParseBigDecimal(true);
        Number number = decimalFormat.parse(feeRate.replace("%", ""));
        // 转换为 BigDecimal 类型并除以 100
        BigDecimal result = new BigDecimal(number.toString()).divide(new BigDecimal("100"));

        botAccountDao.update(null, Wrappers.<BotAccountEntity>lambdaUpdate()
                .eq(BotAccountEntity::getId, botAccount.getId())
                .set(BotAccountEntity::getFeeRate, result)
        );
        return "手续费率设置成功";
    }

    /**
     * 充值
     *
     * @param botAccount
     * @param amount
     * @return
     */
    public String commandCharge(BotAccountEntity botAccount, String amount) {

        boolean usd = false;
        if (amount.endsWith("u")) {
            usd = true;
            amount = amount.substring(0, amount.length() - 1);
        }

        amount = amount.substring(1);
        long la = 0;
        if (!usd) {
            la = (long) Float.parseFloat(amount) * 100;
        } else {
            la = (long) (Float.parseFloat(amount) * botAccount.getUsdRate().doubleValue() * 100);
        }

        long fee = Math.round(la * botAccount.getFeeRate().doubleValue());
        long remain = la - fee;
        log.info("charge:{}, fee:{}, remain:{}", la, fee, remain);

        BotChargeEntity chargeEntity = new BotChargeEntity();
        chargeEntity.setAmount(la);
        chargeEntity.setFee(fee);
        chargeEntity.setFeeRate(botAccount.getFeeRate());
        chargeEntity.setDeptId(botAccount.getDeptId());
        chargeEntity.setUserId(botAccount.getUserId());
        chargeEntity.setUserName(botAccount.getUserName());

        String memo = String.format("充值:%s, 手续费:%sf, 手续费率:%s",
                amount,
                formatDouble(fee / 100.0),
                botAccount.getFeeRate()
        );

        Boolean execute = transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus status) {
                botChargeDao.insert(chargeEntity);
                BotLogEntity logEntity = newLog(
                        botAccount, remain, BledgerConstant.FACT_CHARGE, chargeEntity.getId(), memo);

                int update = botAccountDao.update(null, Wrappers.<BotAccountEntity>lambdaUpdate()
                        .eq(BotAccountEntity::getId, botAccount.getId())
                        .eq(BotAccountEntity::getVersion, botAccount.getVersion())
                        .set(BotAccountEntity::getVersion, botAccount.getVersion() + 1)
                        .set(BotAccountEntity::getBalance, logEntity.getNewAmount())
                );
                if (update == 0) {
                    status.setRollbackOnly();
                    return false;
                }

                return true;
            }
        });
        if (execute == true) {
            return this.commandCheck(botAccount, "check");
        } else {
            return "充值记录失败， 请查询确认并重试";
        }
    }

    /**
     * 提现
     *
     * @param botAccount
     * @param amount
     * @return
     */
    public String commandPayout(BotAccountEntity botAccount, String amount) {
        boolean usd = false;
        if (amount.endsWith("u")) {
            usd = true;
            amount = amount.substring(0, amount.length() - 1);
        }

        amount = amount.substring(1);
        long la;
        if (!usd) {
            la = (long) (Float.parseFloat(amount) * 100);
        } else {
            la = (long) (Float.parseFloat(amount) * botAccount.getUsdRate().doubleValue() * 100);
        }

        BotPayEntity payEntity = new BotPayEntity();
        payEntity.setDeptId(botAccount.getDeptId());
        payEntity.setUserId(botAccount.getUserId());
        payEntity.setUserName(botAccount.getUserName());
        payEntity.setAmount(la);
        String memo = String.format("下发:%s", amount);

        if (!(botAccount.getBalance().longValue() >= la)) {
            return String.format("余额:%s, 下发:%s.  余额不足!!!", botAccount.getBalance() / 100.0, amount);
        }

        Boolean execute = transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus status) {
                botPayDao.insert(payEntity);
                BotLogEntity logEntity = newLog(botAccount, -la, BledgerConstant.FACT_PAYOUT, payEntity.getId(), memo);
                int update = botAccountDao.update(null, Wrappers.<BotAccountEntity>lambdaUpdate()
                        .eq(BotAccountEntity::getId, botAccount.getId())
                        .eq(BotAccountEntity::getVersion, botAccount.getVersion())
                        .set(BotAccountEntity::getVersion, botAccount.getVersion() + 1)
                        .set(BotAccountEntity::getBalance, logEntity.getNewAmount())
                );
                if (update == 0) {
                    return false;
                }
                return true;
            }
        });

        if (execute) {
            return this.commandCheck(botAccount, "check");
        }
        return "下发记账失败";
    }

    public void sendTextMessage(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private BotLogEntity newLog(BotAccountEntity botAccount, Long amount, int factType, Long factId, String factMemo) {
        BotLogEntity logEntity = new BotLogEntity();
        logEntity.setAmount(amount);
        logEntity.setOldAmount(botAccount.getBalance());
        logEntity.setNewAmount(botAccount.getBalance() + amount);
        logEntity.setFactMemo(factMemo);
        logEntity.setFactType(factType);
        logEntity.setFactId(factId);
        logEntity.setDeptId(botAccount.getDeptId());
        logEntity.setUserId(botAccount.getUserId());
        logEntity.setUserName(botAccount.getUserName());
        botLogDao.insert(logEntity);
        return logEntity;
    }

    private String formatDouble(double number) {
        DecimalFormat df = new DecimalFormat("#.00");
        String formattedNumber = df.format(number);
        return formattedNumber;
    }
}
