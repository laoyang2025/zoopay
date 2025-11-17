package io.renren.zbot;

import io.renren.dao.SysUserDao;
import io.renren.zadmin.dao.*;
import io.renren.zapi.UmiOcrService;
import io.renren.zapi.ZConfig;
import io.renren.zapi.card.CardMatchService;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class BotContext {
    @Resource
    private ZConfig config;
    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private ZBotDao botDao;
    @Resource
    private ZBalanceDao balanceDao;
    @Resource
    private ZCardDao cardDao;
    @Resource
    private ZChannelDao channelDao;
    @Resource
    private ZChargeDao chargeDao;
    @Resource
    private UmiOcrService umiOcrService;
    @Resource
    private ZCardLogDao cardLogDao;
    @Resource
    private CardMatchService cardMatchService;
}
