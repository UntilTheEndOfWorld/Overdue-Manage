package com.overdue.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 * 
 * @author Tea Mall Team
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {

    /**
     * OSS服务端点
     */
    private String endPoint;

    /**
     * OSS存储桶名称
     */
    private String bucketName;

    /**
     * 自定义域名（可选）
     */
    private String customDomain;

    /**
     * 文件访问路径前缀
     */
    private String pathPrefix = "";

    /**
     * 是否使用HTTPS
     */
    private boolean useHttps = true;

    /**
     * 连接超时时间（毫秒）
     */
    private int connectionTimeout = 10000;

    /**
     * 最大连接数
     */
    private int maxConnections = 100;

    /**
     * 最大文件大小（字节）
     */
    private long maxFileSize = 10 * 1024 * 1024; // 10MB

    /**
     * 允许的图片格式
     */
    private String[] allowedImageExtensions = {
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"
    };

    /**
     * 允许的视频格式
     */
    private String[] allowedVideoExtensions = {
        "mp4", "avi", "mov", "wmv", "flv", "webm", "mkv"
    };

    /**
     * 允许的文档格式
     */
    private String[] allowedDocumentExtensions = {
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
    };
}