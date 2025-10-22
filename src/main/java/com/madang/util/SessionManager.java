package com.madang.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 세션 관리 클래스 (In-Memory Session Storage)
 * 사용자 로그인 세션을 관리하고 검증
 */
public class SessionManager {

    /**
     * 세션 정보를 담는 내부 클래스
     */
    public static class Session {
        private final int custid;
        private final String name;
        private final String role;
        private final long createdAt;

        public Session(int custid, String name, String role) {
            this.custid = custid;
            this.name = name;
            this.role = role;
            this.createdAt = System.currentTimeMillis();
        }

        public int getCustid() { return custid; }
        public String getName() { return name; }
        public String getRole() { return role; }
        public long getCreatedAt() { return createdAt; }

        public boolean isExpired(long timeoutMillis) {
            return (System.currentTimeMillis() - createdAt) > timeoutMillis;
        }
    }

    // 세션 저장소 (sessionId -> Session)
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    // 세션 타임아웃: 2시간
    private static final long SESSION_TIMEOUT = 2 * 60 * 60 * 1000;

    /**
     * 새로운 세션 생성
     */
    public static String createSession(int custid, String name, String role) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new Session(custid, name, role));
        return sessionId;
    }

    /**
     * 세션 조회
     */
    public static Session getSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        Session session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }

        // 세션 만료 체크
        if (session.isExpired(SESSION_TIMEOUT)) {
            sessions.remove(sessionId);
            return null;
        }

        return session;
    }

    /**
     * 세션 삭제 (로그아웃)
     */
    public static void removeSession(String sessionId) {
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
    }

    /**
     * 만료된 세션 정리 (주기적으로 호출)
     */
    public static void cleanupExpiredSessions() {
        sessions.entrySet().removeIf(entry ->
            entry.getValue().isExpired(SESSION_TIMEOUT)
        );
    }

    /**
     * 세션 수 조회 (디버깅용)
     */
    public static int getSessionCount() {
        return sessions.size();
    }
}
