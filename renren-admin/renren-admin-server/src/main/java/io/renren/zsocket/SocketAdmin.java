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
public class SocketAdmin {

    /**
     * 记录当前在线的Session
     */
    private static final Map<Long, Set<Session>> ONLINE_SESSION = new ConcurrentHashMap<>();

    public static int onlineCount(Long deptId) {
        Set<Session> sessions = ONLINE_SESSION.getOrDefault(deptId,
                new ConcurrentHashSet<>());
        return sessions.size();
    }

    /**
     * 添加session
     *
     * @param deptId
     * @param session
     */
    public static void addSession(Long deptId, Session session) {
        // 此处只允许一个用户的session链接。一个用户的多个连接，我们视为无效。
        ONLINE_SESSION.putIfAbsent(deptId, new ConcurrentHashSet<>());
        Set<Session> sessions = ONLINE_SESSION.get(deptId);
        sessions.add(session);
    }

    /**
     * 关闭session
     *
     * @param deptId
     */
    public static void removeSession(Long deptId, Session session) {
        Set<Session> sessions = ONLINE_SESSION.get(deptId);
        if(sessions != null) {
            sessions.remove(session);
        }
    }

    public static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * send pong
     */
    public static void sendPong(Long deptId) {
        Set<Session> sessions = ONLINE_SESSION.get(deptId);
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
    public static void sendMessage(Long deptId, String msgType, String prompt, Object data) {
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
        log.debug("send websocket msg to dept: {}|{}|{}", deptId, msgType, prompt);
        Set<Session> sessions = ONLINE_SESSION.get(deptId);
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
//        ONLINE_SESSION.forEach((deptId, sessionList) -> sendMessage(deptId, message));
    }

    public static void keepup() {
        ONLINE_SESSION.forEach((deptId, sessionList) -> sendPong(deptId));
    }
}
