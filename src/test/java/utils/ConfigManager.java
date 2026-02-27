package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = ConfigManager.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar config.properties", e);
        }
    }

    public static String get(String key) {
        String env = System.getenv(key.replace(".", "_").toUpperCase());
        if (env != null) return env;
        String sys = System.getProperty(key);
        if (sys != null) return sys;
        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String v = get(key);
        return v != null ? v : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String v = get(key);
        return v != null ? Integer.parseInt(v) : defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String v = get(key);
        return v != null ? Boolean.parseBoolean(v) : defaultValue;
    }
}
