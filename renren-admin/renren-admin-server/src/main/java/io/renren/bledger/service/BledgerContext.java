package io.renren.bledger.service;


import io.renren.bledger.config.BledgerConfig;
import io.renren.bledger.dao.BotAccountDao;
import io.renren.bledger.dao.BotChargeDao;
import io.renren.bledger.dao.BotLogDao;
import io.renren.bledger.dao.BotPayDao;
import io.renren.dao.SysDeptDao;
import io.renren.dao.SysUserDao;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Data
public class BledgerContext {
    @Resource
    private BledgerConfig botmanConfig;
    @Resource
    private BotPayDao botPayDao;
    @Resource
    private BotAccountDao botAccountDao;
    @Resource
    private BotLogDao botLogDao;
    @Resource
    private BotChargeDao botChargeDao;
    @Resource
    private SysUserDao orgUserDao;
    @Resource
    private SysDeptDao orgDeptDao;
    @Resource
    private TransactionTemplate transactionTemplate;

}
