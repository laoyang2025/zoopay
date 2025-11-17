package io.renren.zsocket;

import cn.hutool.crypto.digest.DigestUtil;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.tools.redis.RedisUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.zadmin.dto.ZCardDTO;
import io.renren.zadmin.entity.ZSmsEntity;
import io.renren.zadmin.service.ZCardService;
import io.renren.zadmin.service.ZSmsService;
import io.renren.zapi.AlarmService;
import io.renren.zapi.ZooConstant;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("zms/msg")
@Slf4j
public class SocketMessageController {

    @Resource
    private ZSmsService zSmsService;
    @Resource
    private ZCardService zCardService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private AlarmService alarmService;
    @Resource
    private SocketMessageService socketMessageService;

    @GetMapping("testTask")
    public Result testWebsocketJump() {
        Long deptId = SecurityUser.getDeptId();
        System.out.println("test jump....");
        SocketAdmin.sendMessage(deptId, ZooConstant.MSG_TYPE_TASK, "你有任务需要处理", "/");
        return Result.ok;
    }

    @GetMapping("testWarn")
    public Result testWebsocketWarning() {
        Long deptId = SecurityUser.getDeptId();
        System.out.println("test warning....");
        alarmService.warn(deptId, ZooConstant.MSG_TYPE_WARN, "系统出现异常， 请及时查看");
        return Result.ok;
    }

    @GetMapping("testSms")
    public Result testWebsocktSms() {
        Long deptId = SecurityUser.getDeptId();
        String key = socketMessageService.adminSmsKey(deptId, "18890909090", 20L, null);
        socketMessageService.getMessageByKey(key, 20L);
        return Result.ok;
    }

    @GetMapping("testCaptcha")
    public Result testWebsocketCaptcha() {
        Long deptId = SecurityUser.getDeptId();
        String image = "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";
        String key = socketMessageService.adminCaptchaKey(deptId, 20L, image);
        socketMessageService.getMessageByKey(key, 20L);
        return Result.ok;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 然后通过key来取验证码
     */
    @GetMapping("getSms")
    public Result<String> getSms(@RequestParam("key") String key) {
        return socketMessageService.getMessageByKey(key, 2L);
    }

    /**
     * 验证码打码对外接口
     */
    @GetMapping("getCaptcha")
    public Result<String> getCaptcha(@RequestParam("key") String key) {
        return socketMessageService.getMessageByKey(key, 2L);
    }

    /**
     * 短信打码, 对外接口, 先返回key
     */
    @GetMapping("getSmsKey")
    public Result<String> getSmsKey(
            @RequestParam("deptId") Long deptId,
            @RequestParam("phone") String phone,
            @RequestParam("timeout") Long timeout,
            @RequestParam(value = "prompt", required = false) String prompt
    ) {
        String key = socketMessageService.adminSmsKey(deptId, phone, timeout, prompt);
        Result<String> result = new Result<>();
        result.setData(key);
        return result;
    }

    /**
     * 自动短信时候， 需要先清理phone key里的短信内容
     *
     * @param phone
     * @return
     */
    @GetMapping("clearSmsKey")
    public Result clearSmsKey(@RequestParam("phone") String phone) {

        while (true) {
            if (redisUtils.rightPopNoWait(phone) == null) {
                break;
            }
        }

        return new Result();

    }

    @Data
    public static class SMSUpload {
        List<String> msgs;
        String phone;
        String deviceId;
    }


    /**
     * 短信转发器 - 上报短信
     *
     * @param deptId
     * @return
     */
    @PostMapping("reportSms")
    public Result reportSmsKey(@RequestBody SMSUpload smsUpload, @RequestParam("deptId") Long deptId) {
        String phone = smsUpload.getPhone();

        List<ZCardDTO> cardList = zCardService.list(new HashMap<>());

        // 解析, 能解析到, 就放redis
        for (String msg : smsUpload.getMsgs()) {
            // 入库
            ZSmsEntity zSmsEntity = new ZSmsEntity();
            zSmsEntity.setContent(msg);
            zSmsEntity.setPhone(phone);
            zSmsEntity.setDeptId(deptId);
            zSmsEntity.setMd5(DigestUtil.md5Hex(msg));
            zSmsEntity.setDeviceId(smsUpload.deviceId);

            // 是否是OTP
            socketMessageService.setOtp(zSmsEntity, cardList);

            // 是否可以用作流水回调
            socketMessageService.setUtr(zSmsEntity);

            try {
                zSmsService.insert(zSmsEntity);
                if (zSmsEntity.getUtr() != null) {
                    socketMessageService.matchCollect(zSmsEntity);
                }
            } catch (DuplicateKeyException ex) {
            }
        }

        return new Result();
    }

    /**
     * 验证码打码对外接口
     */
    @PostMapping("getCaptchaKey")
    public Result<String> getCaptchaKey(@RequestBody Map<String, String> body) {
        Long deptId = Long.parseLong(body.get("deptId"));
        Long timeout = null;
        if (body.get("timeout") == null) {
            timeout = 120L;
        } else {
            Long.parseLong(body.get("timeout"));
        }
        String key = socketMessageService.adminCaptchaKey(deptId, timeout, body.get("image"));
        Result<String> result = new Result<>();
        result.setData(key);
        return result;
    }

    /**
     * 人工在管理后台输入短信验证码后,  push到redis消息队列
     */
    @GetMapping("callback/sms/{mykey}")
    public Result smsCallback(@PathVariable("mykey") String mykey, @RequestParam(value = "otp", required = false) String otp) {
        if (otp == null) {
            return Result.fail(9999, "no otp");
        }
        redisUtils.leftPush(mykey, otp);
        redisUtils.expire(mykey, 120);
        return Result.ok;
    }

    /**
     * 人工在管理后台输入captcha后,  push到redis消息队列
     */
    @GetMapping("callback/captcha/{mykey}")
    public Result captchaCallback(@PathVariable("mykey") String mykey, @RequestParam(value = "otp", required = false) String captcha) {
        if (captcha != null) {
            log.debug("push captcha[{}] to key[{}]", captcha, mykey);
            redisUtils.leftPush(mykey, captcha);
            redisUtils.expire(mykey, 120);
        }
        return Result.ok;
    }

    // 上传图片, umi识别为otp
    @GetMapping("reportPicture")
    public Result reportPictureKey(@RequestParam("deptId") Long deptId, @RequestParam("fileId") String fileId, @RequestParam("cardCode") String cardCode) {
        socketMessageService.reportPictureKey(deptId, fileId, cardCode);
        return new Result();
    }

    @GetMapping("clearTotpKey")
    public Result reportPictureKey(@RequestParam("deptId") Long deptId, @RequestParam("phone") String phone) {
        // 同时清理掉队列
        while (true) {
            if (redisUtils.rightPopNoWait(phone) == null) {
                break;
            }
        }
        // 告诉拍照程序， 需要拍照上传了
        totpTask.put(phone, Boolean.TRUE);
        return new Result();
    }

    // 拍照程序， 取任务
    @GetMapping("getPictureTask")
    public Result getPictureTask(@RequestParam("phone") String phone) {
        Boolean aBoolean = totpTask.get(phone);
        if (aBoolean) {
            totpTask.put(phone, Boolean.FALSE);
            return new Result();
        }
        return Result.fail(9999, "no task");
    }

    // 拍照任务记录
    public static ConcurrentHashMap<String, Boolean> totpTask = new ConcurrentHashMap<>();

}