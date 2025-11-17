package io.renren.zapi.controller;


import cn.hutool.core.codec.Base64;
import com.bstek.ureport.console.cache.ObjectMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.zapi.channel.ChannelCallbackService;
import io.renren.zapi.channel.ChannelFactory;
import io.renren.zapi.channel.PayChannel;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("zchannel")
@Slf4j
public class ChannelController {

    @Resource
    private ChannelCallbackService channelCallbackService;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 收到渠道的收款回调 - POST
     *
     * @param deptId
     * @param channelId
     * @param id
     * @param contentType
     * @param body
     * @param response
     */
    @PostMapping(value = "charge/{deptId}/{channelId}/{id}")
    public void chargeNotifiedPost(
            @PathVariable("deptId") Long deptId,
            @PathVariable("channelId") Long channelId,
            @PathVariable("id") Long id,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody String body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        channelCallbackService.doCharge(deptId, channelId, id, contentType, body, request, response);
    }

    /**
     * 收到渠道的收款回调 - GET
     *
     * @param deptId
     * @param channelId
     * @param id
     * @param contentType
     * @param body
     * @param response
     */
    @GetMapping(value = "charge/{deptId}/{channelId}/{id}")
    public void chargeNotifiedGet(
            @PathVariable("deptId") Long deptId,
            @PathVariable("channelId") Long channelId,
            @PathVariable("id") Long id,
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestParam Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        channelCallbackService.doCharge(deptId, channelId, id, contentType, body, request, response);
    }

    /**
     * 收到渠道的代付回调 - POST
     *
     * @param deptId
     * @param channelId
     * @param id
     * @param body
     * @param contentType
     * @param response
     */
    @PostMapping(value = "withdraw/{deptId}/{channelId}/{id}")
    public void drawNotifiedPost(
            @PathVariable("deptId") Long deptId,
            @PathVariable("channelId") Long channelId,
            @PathVariable("id") Long id,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody String body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        channelCallbackService.doWithdraw(deptId, channelId, id, body, contentType, request, response);
    }

    /**
     * 收到渠道的代付回调 - GET
     *
     * @param deptId
     * @param channelId
     * @param id
     * @param body
     * @param contentType
     * @param response
     */
    @GetMapping("withdraw/{deptId}/{channelId}/{id}")
    public void drawNotifiedGet(
            @PathVariable("deptId") Long deptId,
            @PathVariable("channelId") Long channelId,
            @PathVariable("id") Long id,
            @RequestParam Map<String, String> body,
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        channelCallbackService.doWithdraw(deptId, channelId, id, body, contentType, request, response);
    }

    @PostMapping("webhook/{deptId}/{channelId}")
    public void webhook(
            @PathVariable("deptId") Long deptId,
            @PathVariable("channelId") Long channelId,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody String body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        channelCallbackService.doWebhook(deptId, channelId, contentType, body, request, response);
    }

    @GetMapping("test")
    public String test() {
        return "hello";
    }


    @Resource
    private ChannelFactory channelFactory;

    public static TypeReference<Map<String, String>> jumpTypeRef =  new TypeReference<Map<String, String>>() {};
    @GetMapping(value = "jump/{channelId}", produces = MediaType.TEXT_HTML_VALUE)
    public String jump(@PathVariable("channelId") Long channelId,  @RequestParam("data") String data) throws JsonProcessingException {
        String jsonStr = Base64.decodeStr(data);
        log.info("get json: {}", jsonStr);
        Map<String, String> map = objectMapper.readValue(jsonStr, jumpTypeRef);
        PayChannel payChannel = channelFactory.get(channelId);
        return payChannel.jumpHandle(map);
    }
}
