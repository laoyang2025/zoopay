package io.renren.zsocket;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.commons.tools.exception.RenException;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class SocketAnt {

    /**
     * 记录当前在线的Session
     */
    private static final Map<Long, Set<Session>> ONLINE_SESSION = new ConcurrentHashMap<>();

    public static int onlineCount(Long userId) {
        Set<Session> sessions = ONLINE_SESSION.getOrDefault(userId,
                new ConcurrentHashSet<>());
        return sessions.size();
    }

    /**
     * 添加session
     * @param userId
     * @param session
     */
    public static void addSession(Long userId, Session session) {
        ONLINE_SESSION.putIfAbsent(userId, new ConcurrentHashSet<>());
        Set<Session> sessions = ONLINE_SESSION.get(userId);
        sessions.add(session);
    }

    /**
     * 关闭session
     * @param userId
     */
    public static void removeSession(Long userId, Session session) {
        ONLINE_SESSION.get(userId).remove(session);
    }

    public static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * send pong
     */
    public static void sendPong(Long userId) {
        Set<Session> sessions = ONLINE_SESSION.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.forEach(session -> {
            RemoteEndpoint.Async async = session.getAsyncRemote();
            try {
                async.sendPong(ByteBuffer.wrap("pong".getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 给单个用户推送消息
     */
    public static void sendMessage(Long userId, String msgType, String prompt, Object data) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("msgType", msgType);
        map.put("prompt", prompt);
        map.put("data", data);
        String socketMessage;
        try {
            socketMessage = objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RenException("can not gen socket message");
        }
        log.info("send websocket msg to dept: {}", userId);
        Set<Session> sessions = ONLINE_SESSION.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.forEach(session -> {
            RemoteEndpoint.Async async = session.getAsyncRemote();
            async.sendText(socketMessage);
        });
    }

    /**
     * 向所有在线人发送消息
     *
     * @param message
     */
    public static void sendMessageForAll(String message) {
        //jdk8 新方法
//        ONLINE_SESSION.forEach((userId, sessionList) -> sendMessage(userId, message));
    }

    public static void keepup() {
        ONLINE_SESSION.forEach((userId, sessionList) -> sendPong(userId));
    }
}
