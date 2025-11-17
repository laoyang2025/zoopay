package io.renren.zsocket;


import io.renren.zapi.AlarmService;
import io.renren.zapi.utils.CommonUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.*;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * websocket接口处理类
 */
@Component
@ServerEndpoint(value = "/zms/org/{deptId}")
@Slf4j
public class SocketAdminController {


    /**
     * 连接事件，加入注解
     */
    @OnOpen
    public void onOpen(@PathParam(value = "deptId") Long deptId, Session session) {
        log.debug("[{}] - websocket connected", deptId);
        SocketAdmin.addSession(deptId, session);
    }

    /**
     * 连接事件，加入注解
     * 用户断开链接
     *
     * @param deptId
     * @param session
     */
    @OnClose
    public void onClose(@PathParam(value = "deptId") Long deptId, Session session) {
//        log.debug("[{}] - websocket closed", deptId);
        SocketAdmin.removeSession(deptId, session);
    }

    /**
     * 当接收到用户上传的消息
     *
     * @param deptId
     * @param session
     */
    @OnMessage
    public void onMessage(@PathParam(value = "deptId") Long deptId, Session session, String message) {
//        log.debug("[{}] - receive message: {}", deptId, message);

//        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();
//        try {
//            asyncRemote.sendPong(ByteBuffer.wrap(message.getBytes()));
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }

    }

    /**
     * 处理用户活连接异常
     *
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(@PathParam(value = "deptId") Long deptId, Session session, Throwable throwable) {
//        log.debug("[{}] - websocket error: {}", deptId, throwable);
        try {
            SocketAdmin.removeSession(deptId, session);
            session.close();
        } catch (IOException e) {
            // e.printStackTrace();
        }
        // throwable.printStackTrace();
    }


}
