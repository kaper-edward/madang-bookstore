package com.madang.util;

import java.util.Arrays;

public class SqlLogger {

    /**
     * SELECT 쿼리를 포맷하여 콘솔에 로그로 남깁니다.
     * @param sql    실행할 SQL 문자열
     * @param params SQL에 바인딩될 파라미터
     */
    public static void logQuery(String sql, Object... params) {
        System.out.println(formatSql(sql, "QUERY", params));
    }

    /**
     * INSERT, UPDATE, DELETE 쿼리를 포맷하여 콘솔에 로그로 남깁니다.
     * @param sql    실행할 SQL 문자열
     * @param params SQL에 바인딩될 파라미터
     */
    public static void logUpdate(String sql, Object... params) {
        System.out.println(formatSql(sql, "UPDATE", params));
    }

    /**
     * SQL 쿼리 문자열을 읽기 쉽게 포맷팅하는 내부 메서드입니다.
     * @param sql    포맷팅할 원본 SQL 문자열
     * @param type   쿼리 종류 (e.g., "QUERY", "UPDATE")
     * @param params 바인딩될 파라미터
     * @return 포맷팅된 전체 로그 문자열
     */
    private static String formatSql(String sql, String type, Object... params) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }

        // 주요 SQL 키워드 앞에서 줄바꿈과 들여쓰기를 추가합니다.
        // (?i)는 대소문자를 무시하는 옵션, \b는 단어 경계를 의미합니다.
        String formattedSql = sql.trim()
                .replaceAll("(?i)\\b(FROM|LEFT JOIN|RIGHT JOIN|INNER JOIN|WHERE|GROUP BY|ORDER BY|LIMIT)\\b", "\n    $1")
                .replaceAll("(?i)\\b(ON)\\b", "\n        $1");

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n┌───────────────── SQL Log ─────────────────┐");
        logBuilder.append("\n│ [TYPE] ").append(type);
        logBuilder.append("\n│ [SQL]  ").append(formattedSql.replaceAll("\n", "\n│        "));
        if (params != null && params.length > 0) {
            logBuilder.append("\n│ [PARAMS] ").append(Arrays.toString(params));
        }
        logBuilder.append("\n└─────────────────────────────────────────────┘");

        return logBuilder.toString();
    }
}