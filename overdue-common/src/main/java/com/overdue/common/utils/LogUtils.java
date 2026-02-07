package com.overdue.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 日志工具类
 * 提供统一的日志记录功能和格式化
 * 
 * @author Tea Mall Team
 */
public class LogUtils {

    /**
     * 获取Logger实例
     * 
     * @param clazz 类
     * @return Logger实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * 获取Logger实例
     * 
     * @param name Logger名称
     * @return Logger实例
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    /**
     * 记录业务操作日志
     * 
     * @param logger Logger实例
     * @param operation 操作名称
     * @param result 操作结果
     * @param details 详细信息
     */
    public static void logBusiness(Logger logger, String operation, String result, Object details) {
        try {
            MDC.put("operation", operation);
            MDC.put("result", result);
            
            if (details != null) {
                logger.info("业务操作: {} | 结果: {} | 详情: {}", operation, result, details);
            } else {
                logger.info("业务操作: {} | 结果: {}", operation, result);
            }
        } finally {
            MDC.clear();
        }
    }

    /**
     * 记录文件上传日志
     * 
     * @param logger Logger实例
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param uploadResult 上传结果
     * @param timeCost 耗时（毫秒）
     */
    public static void logFileUpload(Logger logger, String fileName, long fileSize, 
                                   String uploadResult, long timeCost) {
        try {
            MDC.put("operation", "FILE_UPLOAD");
            MDC.put("fileName", fileName);
            MDC.put("fileSize", String.valueOf(fileSize));
            MDC.put("timeCost", String.valueOf(timeCost));
            
            logger.info("文件上传: {} | 大小: {}字节 | 结果: {} | 耗时: {}ms", 
                       fileName, fileSize, uploadResult, timeCost);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 记录文件删除日志
     * 
     * @param logger Logger实例
     * @param fileUrl 文件URL
     * @param deleteResult 删除结果
     */
    public static void logFileDelete(Logger logger, String fileUrl, String deleteResult) {
        try {
            MDC.put("operation", "FILE_DELETE");
            MDC.put("fileUrl", fileUrl);
            
            logger.info("文件删除: {} | 结果: {}", fileUrl, deleteResult);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 记录API调用日志
     * 
     * @param logger Logger实例
     * @param method HTTP方法
     * @param url 请求URL
     * @param params 请求参数
     * @param responseTime 响应时间（毫秒）
     * @param statusCode 状态码
     */
    public static void logApiCall(Logger logger, String method, String url, Object params, 
                                long responseTime, int statusCode) {
        try {
            MDC.put("operation", "API_CALL");
            MDC.put("method", method);
            MDC.put("url", url);
            MDC.put("responseTime", String.valueOf(responseTime));
            MDC.put("statusCode", String.valueOf(statusCode));
            
            if (params != null) {
                logger.info("API调用: {} {} | 参数: {} | 响应时间: {}ms | 状态码: {}", 
                           method, url, params, responseTime, statusCode);
            } else {
                logger.info("API调用: {} {} | 响应时间: {}ms | 状态码: {}", 
                           method, url, responseTime, statusCode);
            }
        } finally {
            MDC.clear();
        }
    }

    /**
     * 记录错误日志
     * 
     * @param logger Logger实例
     * @param operation 操作名称
     * @param error 错误信息
     * @param exception 异常对象
     */
    public static void logError(Logger logger, String operation, String error, Throwable exception) {
        try {
            MDC.put("operation", operation);
            MDC.put("error", error);
            
            if (exception != null) {
                logger.error("操作失败: {} | 错误: {} | 异常: {}", operation, error, exception.getMessage(), exception);
            } else {
                logger.error("操作失败: {} | 错误: {}", operation, error);
            }
        } finally {
            MDC.clear();
        }
    }

    /**
     * 记录性能日志
     * 
     * @param logger Logger实例
     * @param operation 操作名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param additionalInfo 附加信息
     */
    public static void logPerformance(Logger logger, String operation, long startTime, 
                                    long endTime, Object additionalInfo) {
        long duration = endTime - startTime;
        
        try {
            MDC.put("operation", operation);
            MDC.put("duration", String.valueOf(duration));
            
            if (additionalInfo != null) {
                logger.info("性能统计: {} | 耗时: {}ms | 附加信息: {}", operation, duration, additionalInfo);
            } else {
                logger.info("性能统计: {} | 耗时: {}ms", operation, duration);
            }
        } finally {
            MDC.clear();
        }
    }

    /**
     * 记录用户操作日志
     * 
     * @param logger Logger实例
     * @param userId 用户ID
     * @param username 用户名
     * @param operation 操作名称
     * @param ip IP地址
     * @param userAgent 用户代理
     */
    public static void logUserOperation(Logger logger, String userId, String username, 
                                      String operation, String ip, String userAgent) {
        try {
            MDC.put("operation", "USER_OPERATION");
            MDC.put("userId", userId);
            MDC.put("username", username);
            MDC.put("ip", ip);
            
            logger.info("用户操作: {} | 用户: {}({}) | IP: {} | UserAgent: {}", 
                       operation, username, userId, ip, userAgent);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 记录系统启动日志
     * 
     * @param logger Logger实例
     * @param applicationName 应用名称
     * @param version 版本号
     * @param startTime 启动时间
     */
    public static void logSystemStart(Logger logger, String applicationName, String version, long startTime) {
        try {
            MDC.put("operation", "SYSTEM_START");
            MDC.put("applicationName", applicationName);
            MDC.put("version", version);
            
            logger.info("系统启动: {} | 版本: {} | 启动时间: {}ms", applicationName, version, startTime);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 记录数据库操作日志
     * 
     * @param logger Logger实例
     * @param operation 操作类型（SELECT, INSERT, UPDATE, DELETE）
     * @param table 表名
     * @param affectedRows 影响行数
     * @param executionTime 执行时间（毫秒）
     */
    public static void logDatabaseOperation(Logger logger, String operation, String table, 
                                          int affectedRows, long executionTime) {
        try {
            MDC.put("operation", "DB_OPERATION");
            MDC.put("dbOperation", operation);
            MDC.put("table", table);
            MDC.put("affectedRows", String.valueOf(affectedRows));
            MDC.put("executionTime", String.valueOf(executionTime));
            
            logger.info("数据库操作: {} | 表: {} | 影响行数: {} | 执行时间: {}ms", 
                       operation, table, affectedRows, executionTime);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 格式化文件大小
     * 
     * @param bytes 字节数
     * @return 格式化后的大小
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        
        return String.format("%.2f %s", 
                           bytes / Math.pow(1024, digitGroups), 
                           units[digitGroups]);
    }

    /**
     * 获取调用者信息
     * 
     * @return 调用者类名和方法名
     */
    public static String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            StackTraceElement caller = stackTrace[3];
            return caller.getClassName() + "." + caller.getMethodName() + ":" + caller.getLineNumber();
        }
        return "Unknown";
    }

    /**
     * 记录方法进入日志
     * 
     * @param logger Logger实例
     * @param methodName 方法名
     * @param params 参数
     */
    public static void logMethodEnter(Logger logger, String methodName, Object... params) {
        if (logger.isDebugEnabled()) {
            if (params != null && params.length > 0) {
                logger.debug("进入方法: {} | 参数: {}", methodName, params);
            } else {
                logger.debug("进入方法: {}", methodName);
            }
        }
    }

    /**
     * 记录方法退出日志
     * 
     * @param logger Logger实例
     * @param methodName 方法名
     * @param result 返回结果
     * @param executionTime 执行时间（毫秒）
     */
    public static void logMethodExit(Logger logger, String methodName, Object result, long executionTime) {
        if (logger.isDebugEnabled()) {
            if (result != null) {
                logger.debug("退出方法: {} | 返回值: {} | 执行时间: {}ms", methodName, result, executionTime);
            } else {
                logger.debug("退出方法: {} | 执行时间: {}ms", methodName, executionTime);
            }
        }
    }

    // ==================== 兼容原有LogUtils方法 ====================

    /**
     * 获取格式化的日志块
     * 用于格式化日志输出，在内容前后添加分隔符
     * 
     * @param msg 消息内容
     * @return 格式化后的字符串
     */
    public static String getBlock(Object msg) {
        if (msg == null) {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }

    /**
     * 获取格式化的日志块（带自定义分隔符）
     * 
     * @param msg 消息内容
     * @param separator 分隔符
     * @return 格式化后的字符串
     */
    public static String getBlock(Object msg, String separator) {
        if (msg == null) {
            msg = "";
        }
        if (separator == null) {
            separator = " ";
        }
        return "[" + msg.toString() + "]" + separator;
    }

    /**
     * 获取格式化的日志块（兼容旧版本）
     * 
     * @param msg 消息内容
     * @return 格式化后的字符串，末尾带空格
     */
    public static String getBlockWithSpace(Object msg) {
        return getBlock(msg, " ");
    }
}