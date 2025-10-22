package com.madang.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 통합 설정 관리 클래스
 *
 * 우선순위: 환경 변수 > application.properties > 기본값
 *
 * 사용 예시:
 * <pre>
 * int port = ConfigManager.getInt("server.port", 8080);
 * String dbUrl = ConfigManager.getString("db.url");
 * </pre>
 */
public class ConfigManager {

    private static final String CONFIG_FILE = "config/application.properties";
    private static Properties properties = new Properties();
    private static boolean initialized = false;

    /**
     * 설정 파일 로드 (싱글톤)
     */
    static {
        loadConfig();
    }

    /**
     * 설정 파일을 로드합니다.
     */
    private static void loadConfig() {
        if (initialized) {
            return;
        }

        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            initialized = true;
            System.out.println("✅ Configuration loaded from " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("⚠️  Warning: Could not load " + CONFIG_FILE + ", using default values");
            System.err.println("   Error: " + e.getMessage());
            // 파일이 없어도 환경 변수와 기본값으로 작동
        }
    }

    /**
     * 문자열 값 조회
     *
     * @param key 설정 키
     * @param defaultValue 기본값
     * @return 설정 값 (환경 변수 > properties > 기본값)
     */
    public static String getString(String key, String defaultValue) {
        // 1순위: 환경 변수 (점(.)을 언더스코어(_)로 변환, 대문자로)
        String envKey = key.replace('.', '_').toUpperCase();
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // 2순위: properties 파일
        String propValue = properties.getProperty(key);
        if (propValue != null && !propValue.isEmpty()) {
            return propValue;
        }

        // 3순위: 기본값
        return defaultValue;
    }

    /**
     * 문자열 값 조회 (기본값 없음)
     */
    public static String getString(String key) {
        return getString(key, null);
    }

    /**
     * 정수 값 조회
     */
    public static int getInt(String key, int defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("⚠️  Warning: Invalid integer value for '" + key + "': " + value);
            return defaultValue;
        }
    }

    /**
     * Long 값 조회
     */
    public static long getLong(String key, long defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("⚠️  Warning: Invalid long value for '" + key + "': " + value);
            return defaultValue;
        }
    }

    /**
     * Boolean 값 조회
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value.trim());
    }

    /**
     * 모든 설정 출력 (디버깅용)
     */
    public static void printConfig() {
        System.out.println("╔═══════════════════════════════════════════════╗");
        System.out.println("║           Application Configuration          ║");
        System.out.println("╠═══════════════════════════════════════════════╣");

        System.out.println("║ Server:");
        System.out.println("║   - Port: " + getInt("server.port", 8080));
        System.out.println("║   - Host: " + getString("server.host", "0.0.0.0"));
        System.out.println("║   - Thread Pool: " + getInt("server.thread.pool.size", 10));
        System.out.println("║   - Frontend Dir: " + getString("server.frontend.dir", "frontend"));

        System.out.println("║ Database:");
        System.out.println("║   - URL: " + getString("db.url", "jdbc:mysql://localhost:3306/madangdb"));
        System.out.println("║   - User: " + getString("db.user", "madang"));
        System.out.println("║   - Password: ****");

        System.out.println("║ Connection Pool:");
        System.out.println("║   - Max Size: " + getInt("db.pool.maximum.size", 10));
        System.out.println("║   - Min Idle: " + getInt("db.pool.minimum.idle", 2));
        System.out.println("║   - Connection Timeout: " + getLong("db.pool.connection.timeout", 30000) + "ms");

        System.out.println("╚═══════════════════════════════════════════════╝");
    }

    /**
     * 설정 다시 로드
     */
    public static void reload() {
        properties.clear();
        initialized = false;
        loadConfig();
    }
}
