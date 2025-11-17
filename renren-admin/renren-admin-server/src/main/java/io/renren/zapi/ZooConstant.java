package io.renren.zapi;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.intern.Interner;
import cn.hutool.core.lang.intern.WeakInterner;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZooConstant {
    /**
     * ind 产品
     */
    public static String IND_PAYCODE_UPI = "upi";

    /**
     * chn 产品
     */
    public static String CHN_PAYCODE_ALIPAY = "alipay";
    public static String CHN_PAYCODE_WECHAT = "wechat";
    public static String CHN_PAYCODE_YSF = "ysf";
    public static String CHN_PAYCODE_BANK = "bank";

    /**
     * vnm 产品
     */
    public static String VNM_PAYCODE_CARD = "card";

    /**
     * productMapping
     */
    public static Map<String, List<String>> mapping = new HashMap<>() {{
        put("ind", List.of("upi"));
        put("chn", List.of("alipay", "wechat", "ysf", "bank"));
        put("vnm", List.of("card"));
    }};

    /**
     * 充值状态
     */
    public static final int CHARGE_STATUS_NEW = 0;
    public static final int CHARGE_STATUS_PROCESSING = 1;
    public static final int CHARGE_STATUS_SUCCESS = 2;
    public static final int CHARGE_STATUS_TIMEOUT = 3;


    /**
     * 提现处理状态
     */
    public static final int WITHDRAW_STATUS_NEW = 0;
    public static final int WITHDRAW_STATUS_ASSIGNED = 1;
    public static final int WITHDRAW_STATUS_SUCCESS = 2;
    public static final int WITHDRAW_STATUS_FAIL = 3;

    /**
     * 通知状态, 0: 待通知, 1: 通知成功, 2: 通知失败
     */
    public static final int NOTIFY_STATUS_SUCCESS = 1;
    public static final int NOTIFY_STATUS_TODO = 0;
    public static final int NOTIFY_STATUS_FAIL = 2;


    /**
     * 处理模式
     */
    public static final String PROCESS_MODE_CHANNEL = "channel";
    public static final String PROCESS_MODE_ANT = "ant";
    public static final String PROCESS_MODE_AGENT = "agent";
    public static final String PROCESS_MODE_CARD = "card";

    /**
     * 用户类型
     */
    public static final String USER_TYPE_OPERATION = "operation";  // 机构运营用户
    public static final String USER_TYPE_MERCHANT = "merchant";    // 商户用户
    public static final String USER_TYPE_AGENT = "agent";          // 代理用户
    public static final String USER_TYPE_USER = "user";            // 卡主用户
    public static final String USER_TYPE_ANT = "ant";              // 码农用户
    public static final String USER_TYPE_MIDDLE = "middle";        // 中间介绍人

    /**
     * 内部账户类型
     */
    public static final String OWNER_TYPE_MERCHANT = "merchant";
    public static final String OWNER_TYPE_AGENT = "agent";
    public static final String OWNER_TYPE_AGENT_SHARE = "agent:share";
    public static final String OWNER_TYPE_USER = "user";
    public static final String OWNER_TYPE_ANT = "ant";  // 码农账户
    public static final String OWNER_TYPE_CHANNEL = "channel"; // 渠道记账 没用
    public static final String OWNER_TYPE_BASKET = "basket";   // 公户记账 没用
    public static final String OWNER_TYPE_CARD = "card";       // 自卡记账 没用

    /**
     * 锁资源
     */
    public static final Interner<String> agentLocks = new WeakInterner<>();  // 代理模式调度锁
    public static final Interner<String> antLocks = new WeakInterner<>(); // 码农模式调度锁
    public static final Interner<String> antClaimLocks = new WeakInterner<>(); // 码农模式下, 码农抢代付单的锁
    public static final Interner<String> userClaimLocks = new WeakInterner<>(); // 代理模式下, 卡主代付单的锁
    public static final Interner<String> merchantLocks = new WeakInterner<>();  // 商户出入账的时候的锁

    /**
     *
     */
    public static final Object MATCH_TODO = 0;  // 待匹配
    public static final Object MATCH_SUCCESS = 1;  // 匹配成功
    public static final Object MATCH_FAIL = 2;  // 匹配失败

    public static String getMerchantLock(Long merchantId) {
        return merchantLocks.intern(merchantId.toString());
    }


    // 消息类型
    public static final String MSG_TYPE_WARN = "warn"; // 告警消息
    public static final String MSG_TYPE_TASK = "task";  // 任务消息
    public static final String MSG_TYPE_SMS = "sms"; // 短信消息
    public static final String MSG_TYPE_CAPTCHA = "captcha"; // 验证码消息

    /**
     * 代理跑分
     * key --> cardId ---> success:
     * key --> cardId ---> total:
     * agent:card:online:deptId --> agentId:userid:cardId  ---> timestamp
     */
    public static String agentCardSuccessKey(Long deptId) {
        return "agent:card:success:" + DateUtil.formatDate(new Date()) + ":" + deptId;
    }

    public static String agentCardTotalKey(Long deptId) {
        return "agent:card:total:" + DateUtil.formatDate(new Date()) + ":" + deptId;
    }

    public static String agentCardOnlineKey(Long deptId) {
        return "agent:card:online:" + deptId;
    }

    public static String agentCardOnlineField(Long agentId, Long userId, Long cardId) {
        return agentId + ":" + userId + ":" + cardId;
    }

    /**
     * 码农跑分
     * ant:card:success:date -> number
     * ant:card:total:date -> number
     * ant:card:online:deptId --> antId:cardId  ---> timestamp
     *
     * @param deptId
     * @return
     */
    public static String antCardSuccessKey(Long deptId) {
        return "ant:card:success:" + DateUtil.formatDate(new Date()) + ":" + deptId;
    }

    public static String antCardTotalKey(Long deptId) {
        return "ant:card:total:" + DateUtil.formatDate(new Date()) + ":" + deptId;
    }

    public static String antCardOnlineKey(Long deptId) {
        return "ant:card:online:" + deptId;
    }

    public static String antCardOnlineField(Long antId, Long cardId) {
        return antId + ":" + cardId;
    }

    /**
     * 自营卡计数
     *
     * @param deptId
     * @return
     */
    public static String cardSuccessKey(Long deptId) {
        return "card:success:" + DateUtil.formatDate(new Date()) + ":" + deptId;
    }

    public static String cardTotalKey(Long deptId) {
        return "card:total:" + DateUtil.formatDate(new Date()) + ":" + deptId;
    }

    /**
     * tn匹配用的key
     *
     * @param deptId
     * @return
     */
    public static String cardTnKey(Long deptId) {
        return "card:tn:" + deptId;
    }

    public static String userTnKey(Long deptId) {
        return "user:tn:" + deptId;
    }

    public static String antTnKey(Long deptId) {
        return "user:tn:" + deptId;
    }
}
