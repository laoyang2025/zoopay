package io.renren.zapi.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.tools.redis.RedisUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zapi.agent.AgentTimeoutService;
import io.renren.zapi.merchant.ApiService;
import io.renren.zapi.merchant.dto.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("zapi")
public class ApiController {
    @Resource
    private ApiService apiService;
    @Autowired
    private AgentTimeoutService agentTimeoutService;

    @GetMapping("health")
    public String health() {
        return "OK";
    }

    /**
     * 充值
     *
     * @param body
     * @param sign
     * @param appKey
     * @return
     */
    @PostMapping("charge")
    public Result<ChargeResponse> charge(@RequestBody String body, @RequestHeader(value = "x-sign", required = false) String sign, @RequestHeader(value = "x-app-key", required = false) String appKey) {
        return apiService.charge(body, sign, appKey);
    }

    /**
     * 提现
     *
     * @param body
     * @param sign
     * @param appKey
     * @return
     */
    @PostMapping("withdraw")
    public Result<WithdrawResponse> withdraw(@RequestBody String body, @RequestHeader(value = "x-sign", required = false) String sign, @RequestHeader(value = "x-app-key", required = false) String appKey) {
        return apiService.withdraw(body, sign, appKey);
    }

    /**
     * 充值查询
     *
     * @param body
     * @param sign
     * @param appKey
     * @return
     */
    @PostMapping("chargeQuery")
    public Result<ChargeQueryResponse> chargeQuery(@RequestBody String body, @RequestHeader(value = "x-sign", required = false) String sign, @RequestHeader(value = "x-app-key", required = false) String appKey) {
        return apiService.chargeQuery(body, sign, appKey);
    }


    /**
     * 提现查询
     *
     * @param body
     * @param sign
     * @param appKey
     * @return
     */
    @PostMapping("withdrawQuery")
    public Result<WithdrawQueryResponse> withdrawQuery(@RequestBody String body, @RequestHeader(value = "x-sign", required = false) String sign, @RequestHeader(value = "x-app-key", required = false) String appKey) {
        return apiService.withdrawQuery(body, sign, appKey);
    }

    /**
     * 余额查询
     *
     * @param body
     * @param sign
     * @param appKey
     * @return
     */
    @PostMapping("balance")
    public Result<BalanceResponse> balance(@RequestBody String body, @RequestHeader(value = "x-sign", required = false) String sign, @RequestHeader(value = "x-app-key", required = false) String appKey) {
        return apiService.balance(body, sign, appKey);
    }

    /**
     * 落地页提交utr
     *
     * @param id
     * @param utr
     * @return
     */
    @PostMapping("submitUtr")
    public Result submitUtr(@RequestParam("id") Long id, @RequestParam("utr") String utr) {
        return apiService.submitUtr(id, utr);
    }

    /**
     * 落地页查询充值状态
     */
    @GetMapping("chargeStatus")
    public Result<Integer> chargeStatus(@RequestParam("id") Long id) {
        int status = apiService.chargeStatus(id);
        Result<Integer> result = new Result<>();
        result.setData(status);
        return result;
    }

    /**
     * 商戶联调自动回调收款交易
     *
     * @param id
     * @param status
     * @return
     */
    @GetMapping("chargeDev")
    public Result chargeDev(@RequestParam("id") long id, @RequestParam("status") int status) {
        return apiService.chargeDev(id, status);
    }

    /**
     * 自动糊掉代付交易
     *
     * @param id
     * @param status
     * @return
     */
    @GetMapping("withdrawDev")
    public Result withdrawDev(@RequestParam("id") Long id, @RequestParam("status") int status) {
        return apiService.withdrawDev(id, status);
    }

    /**
     * 演示环境需要
     */
    @GetMapping("merchantBalance")
    public Result<BigDecimal> merchantBalance(@RequestParam("deptId") Long deptId, @RequestParam("merchantId") Long merchantId) {
        return apiService.merchantInfo(deptId, merchantId);
    }



    private static String upiTemplate = null;

    @PostConstruct
    public void postInit() {
        try {
            // 步骤1：从类路径读取upi.html模板文件
            ClassPathResource resource = new ClassPathResource("sync.html");
            // 读取模板文件内容为字符串
            InputStream inputStream = resource.getInputStream();
            byte[] templateBytes = FileCopyUtils.copyToByteArray(inputStream);
            upiTemplate = new String(templateBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
        }

    }



    /**
     *  我方upih5落地页面
     */
    @GetMapping("sync")
    public void upih5(HttpServletResponse response, HttpServletRequest request) {
        // 1. 设置响应头，指定内容类型和编码
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            // 步骤2：从request中获取upi参数
            // 第二个参数为默认值，防止参数为空时出现NullPointerException
            String upiParam = request.getParameter("upi");
            if (upiParam == null || upiParam.trim().isEmpty()) {
                upiParam = ""; // 空参数时替换为空字符串，避免模板中保留{{upi}}
            }
            String realUrl = upiParam.replace("&amp;", "&");
            // 步骤3：替换模板中的{{upi}}变量
            String finalContent = upiTemplate.replace("{{upi}}", realUrl);
            // 步骤4：将替换后的内容写入响应
            response.getWriter().write(finalContent);
            response.getWriter().flush();
        } catch (IOException e) {
            // 异常处理：返回500错误
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "处理支付页面失败：" + e.getMessage());
            } catch (IOException ex) {
                // 记录日志（实际项目中建议使用日志框架如logback/log4j）
                System.err.println("响应错误信息发送失败：" + ex.getMessage());
            }
            // 打印异常栈（生产环境建议改为日志记录）
            e.printStackTrace();
        }
    }

    /**
     *  我方upih5落地页面
     */
    @GetMapping("async")
    public void async(HttpServletResponse response, HttpServletRequest request) {
        // 1. 设置响应头，指定内容类型和编码
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(upiTemplate);
            response.getWriter().flush();
        } catch (IOException e) {
            // 异常处理：返回500错误
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "process error：");
            } catch (IOException ex) {
                // 记录日志（实际项目中建议使用日志框架如logback/log4j）
                System.err.println("响应错误信息发送失败：" + ex.getMessage());
            }
            // 打印异常栈（生产环境建议改为日志记录）
            e.printStackTrace();
        }
    }

    @Resource
    private RedisUtils redisUtils;

    @GetMapping("qrcode")
    public String qrcode(String id, String channel) {
        try {
            String o = (String) redisUtils.rightPop(id, 15);
            if (o != null) {
                return o;
            }
        } catch (Exception e) {
            String o1 = (String)redisUtils.get(id + "-k");
            if (o1 != null) {
                return o1;
            }
            return null;
        }

        String o1 = (String)redisUtils.get(id + "-k");
        if (o1 != null) {
            return o1;
        }

        return null;
    }

    // 同步更新渠道定单号
    @Resource
    private ZChargeDao zChargeDao;
    @GetMapping("report/channel")
    public String reportChannelOrder(@RequestParam("channelOrder") String channelOrder, @RequestParam("id") Long id) {
        zChargeDao.update(Wrappers.<ZChargeEntity>lambdaUpdate()
                .eq(ZChargeEntity::getId, id)
                .set(ZChargeEntity::getChannelOrder, channelOrder)
        );
        return "ok";
    }

}
