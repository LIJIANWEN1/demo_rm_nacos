package com.example.demo1_nacos.antivirus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志统一输出工具类
 *
 * @author hejiandong
 * @date 2021/3/6
 */
public class LogUtil {

    /**
     * 各类Logger缓存
     */
    private static Map<Object, Logger> logCachePool = new ConcurrentHashMap<>(300);

    /**
     * 获取调用类的logger
     */
    public static Logger getCurrentThreadClassLogger() {
        String className = Thread.currentThread().getStackTrace()[3].getClassName();
        return getLogger(className);
    }

    public static Logger getLogger(String loggerName) {
        if (logCachePool.get(loggerName) != null) {
            return logCachePool.get(loggerName);
        }
        Logger logger = LoggerFactory.getLogger(loggerName);
        logCachePool.put(loggerName, logger);
        return logger;
    }

    public static void info(String message, Throwable t) {
        if (getCurrentThreadClassLogger().isInfoEnabled()) {
            getCurrentThreadClassLogger().info(message, t);
        }
    }

    public static void info(String message) {
        if (getCurrentThreadClassLogger().isInfoEnabled()) {
            getCurrentThreadClassLogger().info(message);
        }
    }

    public static void info(String format, Object... arguments) {
        if (getCurrentThreadClassLogger().isInfoEnabled()) {
            getCurrentThreadClassLogger().info(format, arguments);
        }
    }

    public static void info(String format, Object arg1, Object arg2) {
        if (getCurrentThreadClassLogger().isInfoEnabled()) {
            getCurrentThreadClassLogger().info(format, arg1, arg2);
        }
    }

    public static void error(String message) {
        if (getCurrentThreadClassLogger().isErrorEnabled()) {
            getCurrentThreadClassLogger().error(message);
        }
    }

    public static void error(String format, Object arg1, Object arg2) {
        if (getCurrentThreadClassLogger().isErrorEnabled()) {
            getCurrentThreadClassLogger().error(format, arg1, arg2);
        }
    }

    public static void error(String format, Object... arguments) {
        if (getCurrentThreadClassLogger().isErrorEnabled()) {
            getCurrentThreadClassLogger().error(format, arguments);
        }
    }

    public static void error(String message, Throwable t) {
        if (getCurrentThreadClassLogger().isErrorEnabled()) {
            getCurrentThreadClassLogger().error(message, t);
        }
    }

    /**
     * debug level
     *
     * @param message
     * @param t
     */
    public static void debug(String message, Throwable t) {
        getCurrentThreadClassLogger().debug(message, t);
    }

    /**
     * warn level
     *
     * @param message
     * @param t
     */
    public static void warn(String message, Throwable t) {
        getCurrentThreadClassLogger().warn(message, t);
    }

    /**
     * warn level
     *
     * @param message
     */
    public static void warn(String message, Object... format) {
        getCurrentThreadClassLogger().warn(message, format);
    }

    /**
     * debug level
     *
     * @param message
     */
    public static void debug(String message) {
        getCurrentThreadClassLogger().debug(message);
    }

    /**
     * warn level
     *
     * @param message
     */
    public static void warn(String message) {
        getCurrentThreadClassLogger().warn(message);
    }
}

