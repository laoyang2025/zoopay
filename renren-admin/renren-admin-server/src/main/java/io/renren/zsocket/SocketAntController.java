package io.renren.zsocket;


import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * websocket接口处理类
 */
@Component
@ServerEndpoint(value = "/zms/user/{userId}")
@Slf4j
public class SocketAntController {

    /**
     * 连接事件，加入注解
     */
    @OnOpen
    public void onOpen(@PathParam(value = "userId") Long userId, Session session) {
//        System.out.println("user client websocket connected");
        SocketAnt.addSession(userId, session);
    }

    /**
     * 连接事件，加入注解
     * 用户断开链接
     *
     * @param userId
     * @param session
     */
    @OnClose
    public void onClose(@PathParam(value = "userId") Long userId, Session session) {
//        log.info("[{}] - websocket closed", userId);
        SocketAnt.removeSession(userId, session);
    }

    /**
     * 当接收到用户上传的消息
     *
     * @param userId
     * @param session
     */
    @OnMessage
    public void onMessage(@PathParam(value = "userId") Long userId, Session session, String message) {
        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();
        try {
            asyncRemote.sendPong(ByteBuffer.wrap(message.getBytes()));
        } catch (IOException e) {
//            log.error(e.getMessage());
        }
    }

    /**
     * 处理用户活连接异常
     *
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(@PathParam(value = "userId") Long userId, Session session, Throwable throwable) {
        try {
//            log.info("[{}] - websocket error", userId);
            SocketAnt.removeSession(userId, session);
            session.close();
        } catch (IOException e) {
            // e.printStackTrace();
        }
        // throwable.printStackTrace();
    }
}
