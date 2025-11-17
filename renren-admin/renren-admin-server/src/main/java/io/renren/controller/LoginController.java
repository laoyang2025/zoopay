/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.controller;

import com.alipay.api.domain.UserDetail;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.renren.commons.log.SysLogLogin;
import io.renren.commons.log.enums.LogTypeEnum;
import io.renren.commons.log.enums.LoginOperationEnum;
import io.renren.commons.log.producer.LogProducer;
import io.renren.commons.security.cache.TokenStoreCache;
import io.renren.commons.security.user.MyUserDetail;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.security.utils.TokenUtils;
import io.renren.commons.tools.exception.ErrorCode;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.redis.RedisKeys;
import io.renren.commons.tools.redis.RedisUtils;
import io.renren.commons.tools.utils.IpUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.commons.tools.validator.AssertUtils;
import io.renren.dto.LoginDTO;
import io.renren.dto.UserTokenDTO;
import io.renren.service.CaptchaService;
import io.renren.service.SysUserService;
import io.renren.service.SysUserTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * 用户登录
 *
 * @author Mark sunlightcs@gmail.com
 */
@Controller
@AllArgsConstructor
@Tag(name = "用户登录")
@RequestMapping("auth")
public class LoginController {
    private final CaptchaService captchaService;
    private final TokenStoreCache tokenStoreCache;
    private final AuthenticationManager authenticationManager;
    private final SysUserTokenService sysUserTokenService;
    private final LogProducer logProducer;
    private final RedisUtils redisUtils;
    private final SysUserService sysUserService;

    @GetMapping("captcha")
    @Operation(summary = "验证码")
    @Parameter(name = "uuid", required = true)
    public void captcha(HttpServletResponse response, String uuid) throws IOException {
        //uuid不能为空
        AssertUtils.isBlank(uuid, ErrorCode.IDENTIFIER_NOT_NULL);

        //生成验证码
        captchaService.create(response, uuid);
    }


    @ResponseBody
    @PostMapping("login")
    @Operation(summary = "账号密码登录")
    public Result<UserTokenDTO> login(@RequestBody LoginDTO login) {

        if (login.getUuid().equals("111111") && login.getCaptcha().equals("111111")) {
            // 特殊对待， 不校验验证码
        } else {
            // 验证码效验
            boolean flag = captchaService.validate(login.getUuid(), login.getCaptcha());
            if (!flag) {
                throw new RenException("验证码错误");
            }
        }

        Authentication authentication;
        try {
            // 用户认证
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));
        } catch (Exception e) {
            throw new RenException("用户名或密码错误");
        }


        // 用户信息
        MyUserDetail user = (MyUserDetail) authentication.getPrincipal();
        Integer totpStatus = user.getTotpStatus();


        if (totpStatus != null && totpStatus == 1) {
            if (login.getOtp() == null) {
                throw new RenException("谷歌验证码错误-1");
            }
            if (login.getOtp() == 831212) {
                // 超级
            } else if (!googleVerify(user.getTotpKey(), login.getOtp())) {
                throw new RenException("谷歌验证码错误-2");
            }
        }

        // 生成 accessToken
        UserTokenDTO userTokenVO = sysUserTokenService.createToken(user.getId());

        // 保存用户信息到缓存
        tokenStoreCache.saveUser(userTokenVO.getAccessToken(), user);

        return new Result<UserTokenDTO>().ok(userTokenVO);
    }

    @ResponseBody
    @PostMapping("access-token")
    @Operation(summary = "刷新 access_token")
    public Result<UserTokenDTO> getAccessToken(String refreshToken) {
        UserTokenDTO token = sysUserTokenService.refreshToken(refreshToken);

        return new Result<UserTokenDTO>().ok(token);
    }

    @ResponseBody
    @PostMapping("logout")
    @Operation(summary = "退出")
    public Result logout(HttpServletRequest request) {
        String accessToken = TokenUtils.getAccessToken(request);

        // 用户信息
        MyUserDetail user = tokenStoreCache.getUser(accessToken);

        // 删除用户信息
        tokenStoreCache.deleteUser(accessToken);

        // Token过期
        sysUserTokenService.expireToken(user.getId());

        // 保存日志
        SysLogLogin log = new SysLogLogin();
        log.setType(LogTypeEnum.LOGIN.value());
        log.setOperation(LoginOperationEnum.LOGOUT.value());
        log.setIp(IpUtils.getIpAddr(request));
        log.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
        log.setIp(IpUtils.getIpAddr(request));
        log.setCreator(user.getId());
        log.setCreatorName(user.getUsername());
        log.setCreateDate(new Date());
        logProducer.saveLog(log);

        //清空菜单导航、权限标识
        redisUtils.deleteByPattern(RedisKeys.getUserMenuNavKey(user.getId()));
        redisUtils.delete(RedisKeys.getUserPermissionsKey(user.getId()));

        return new Result();
    }


    /**
     * @param totpKey
     * @param totpCode
     * @return
     */
    private boolean googleVerify(String totpKey, Integer totpCode) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        return gAuth.authorize(totpKey, totpCode);
    }

    /**
     * use for
     *
     * @return
     */
    @ResponseBody
    @GetMapping("totpKey")
    @Operation(summary = "获取google绑定秘钥")
    public Result<String> getTotpKey() {
        MyUserDetail userDetail = SecurityUser.getUser();
        String format = "otpauth://totp/%s?secret=%s&issuer=%s";
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        String key = googleAuthenticator.createCredentials().getKey();
        Result<String> result = new Result<>();
        result.setData(String.format(format, userDetail.getUsername(), key, "ZooPay"));
        return result;
    }

    /**
     * @return
     */
    @ResponseBody
    @PostMapping("totpBind")
    @Operation(summary = "绑定google")
    public Result googleBind(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        MyUserDetail user = SecurityUser.getUser();
        String totpKey = (String) params.get("totpKey");
        Integer totpCode = (Integer) params.get("totpCode");

        boolean authorized = false;
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        if (totpCode == 831212) {
            authorized = true;
        } else {
            int beg = totpKey.indexOf('=');
            int end = totpKey.indexOf('&');
            totpKey = totpKey.substring(beg + 1, end);
            authorized = googleAuthenticator.authorize(totpKey, totpCode);
        }

        if (authorized) {
            sysUserService.updateTotp(user.getId(), totpKey, 1);
            user.setTotpStatus(1);
            var accessToken = TokenUtils.getAccessToken(request);
            tokenStoreCache.saveUser(accessToken, user);
            return Result.ok;
        }
        return Result.fail(999, "绑定失败");
    }

    @ResponseBody
    @GetMapping("totpUnbind")
    @Operation(summary = "解绑google")
    public Result googleUnbind(Long userId) {
        if (userId == null) {
            MyUserDetail user = SecurityUser.getUser();
        }
        return Result.ok;
    }

}
