package com.overdue.common.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.*;
import com.aliyun.oss.model.*;
import com.overdue.common.exception.ServiceException;
import com.overdue.common.exception.FileUploadException;
import com.overdue.common.utils.uuid.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 阿里云OSS工具类
 * 提供文件上传、下载、删除等功能
 * 
 * @author Tea Mall Team
 */
@Slf4j
@Component
public class OssUtils {

    @Value("${aliyun.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.secretAccessKey}")
    private String secretAccessKey;

    @Value("${aliyun.oss.endPoint}")
    private String endPoint;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.pathPrefix:}")
    private String pathPrefix;

    @Value("${aliyun.oss.useHttps:true}")
    private boolean useHttps;

    @Value("${aliyun.oss.customDomain:}")
    private String customDomain;

    @Value("${aliyun.oss.connectionTimeout:10000}")
    private int connectionTimeout;

    @Value("${aliyun.oss.maxConnections:100}")
    private int maxConnections;

    // 支持的图片格式
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg");

    // 支持的视频格式
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(
            "mp4", "avi", "mov", "wmv", "flv", "webm", "mkv");

    // 支持的文档格式
    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt");

    // 最大文件大小（10MB）
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @PostConstruct
    public void init() {
        LogUtils.logBusiness(log, "OSS_INIT", "SUCCESS",
                String.format("EndPoint: %s, BucketName: %s, PathPrefix: %s, UseHttps: %s, CustomDomain: %s",
                        endPoint, bucketName, pathPrefix, useHttps, customDomain));
    }

    /**
     * 创建OSS客户端
     */
    private OSS createOSSClient() {
        ClientBuilderConfiguration config = new ClientBuilderConfiguration();
        config.setConnectionTimeout(connectionTimeout);
        config.setMaxConnections(maxConnections);
        config.setSupportCname(StrUtil.isNotBlank(customDomain));

        return new OSSClientBuilder().build(endPoint, accessKeyId, secretAccessKey, config);
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw FileUploadException.fileEmpty();
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw FileUploadException.fileTooLarge(MAX_FILE_SIZE);
        }

        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw FileUploadException.invalidFileName();
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!isAllowedExtension(extension)) {
            throw FileUploadException.invalidFileType(extension);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (StrUtil.isBlank(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    /**
     * 检查是否为允许的文件扩展名
     */
    private boolean isAllowedExtension(String extension) {
        return IMAGE_EXTENSIONS.contains(extension) ||
                VIDEO_EXTENSIONS.contains(extension) ||
                DOCUMENT_EXTENSIONS.contains(extension);
    }

    /**
     * 生成文件路径
     */
    private String generateFilePath(String originalFilename, String category) {
        String extension = getFileExtension(originalFilename);
        String datePath = new DateTime().toString("yyyy/MM/dd");
        String uuid = UUID.randomUUID().toString().replace("-", "");

        StringBuilder pathBuilder = new StringBuilder();

        // 添加路径前缀
        if (StrUtil.isNotBlank(pathPrefix)) {
            pathBuilder.append(pathPrefix);
            if (!pathPrefix.endsWith("/")) {
                pathBuilder.append("/");
            }
        }

        // 添加分类目录
        if (StrUtil.isNotBlank(category)) {
            pathBuilder.append(category).append("/");
        }

        // 添加日期路径
        pathBuilder.append(datePath).append("/");

        // 添加文件名
        pathBuilder.append(uuid);
        if (StrUtil.isNotBlank(extension)) {
            pathBuilder.append(".").append(extension);
        }

        return pathBuilder.toString();
    }

    /**
     * 构建文件访问URL
     */
    private String buildFileUrl(String objectName) {
        if (StrUtil.isNotBlank(customDomain)) {
            String protocol = useHttps ? "https" : "http";
            return protocol + "://" + customDomain + "/" + objectName;
        } else {
            String protocol = useHttps ? "https" : "http";
            return protocol + "://" + bucketName + "." + endPoint + "/" + objectName;
        }
    }

    /**
     * 下载文件到本地
     */
    public void downloadFile(String objectName, String localPath) {
        OSS ossClient = createOSSClient();
        try {
            // 提取文件名
            String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
            String fullPath = localPath + File.separator + fileName;

            // 下载文件
            ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File(fullPath));
            LogUtils.logBusiness(log, "FILE_DOWNLOAD", "SUCCESS",
                    String.format("从 %s 下载到 %s", objectName, fullPath));
        } catch (OSSException | ClientException e) {
            LogUtils.logError(log, "FILE_DOWNLOAD", "下载文件失败: " + objectName, e);
            throw new ServiceException("文件下载失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 上传单个文件
     * 
     * @param file 要上传的文件
     * @return 文件访问URL
     */
    public String uploadOneFile(MultipartFile file) {
        return uploadOneFile(file, null);
    }

    /**
     * 上传单个文件到指定分类目录
     * 
     * @param file     要上传的文件
     * @param category 分类目录（如：images、documents、videos）
     * @return 文件访问URL
     */
    public String uploadOneFile(MultipartFile file, String category) {
        // 验证文件
        validateFile(file);

        OSS ossClient = createOSSClient();
        try {
            // 生成文件路径
            String objectName = generateFilePath(file.getOriginalFilename(), category);

            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName, objectName, file.getInputStream());

            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            metadata.setCacheControl("max-age=31536000"); // 缓存一年
            putObjectRequest.setMetadata(metadata);

            // 上传文件
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            // 构建访问URL
            String fileUrl = buildFileUrl(objectName);

            log.info("文件上传成功: {} -> {}", file.getOriginalFilename(), fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("文件上传失败: {}", file.getOriginalFilename(), e);
            throw new ServiceException("文件上传失败: " + e.getMessage());
        } catch (OSSException | ClientException e) {
            log.error("OSS上传失败: {}", file.getOriginalFilename(), e);
            throw new ServiceException("文件上传失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 批量上传文件
     * 
     * @param files 要上传的文件数组
     * @return 文件访问URL列表
     */
    public List<String> uploadArrayFile(MultipartFile[] files) {
        return uploadArrayFile(files, null);
    }

    /**
     * 批量上传文件到指定分类目录
     * 
     * @param files    要上传的文件数组
     * @param category 分类目录
     * @return 文件访问URL列表
     */
    public List<String> uploadArrayFile(MultipartFile[] files, String category) {
        if (files == null || files.length == 0) {
            throw new ServiceException("文件列表不能为空");
        }

        List<String> urlList = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String url = uploadOneFile(file, category);
                urlList.add(url);
            } catch (Exception e) {
                log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                failedFiles.add(file.getOriginalFilename());
            }
        }

        if (!failedFiles.isEmpty()) {
            log.warn("部分文件上传失败: {}", failedFiles);
            throw new ServiceException("部分文件上传失败: " + String.join(", ", failedFiles));
        }

        log.info("批量文件上传成功，共{}个文件", urlList.size());
        return urlList;
    }

    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            log.warn("文件URL为空，跳过删除");
            return false;
        }

        OSS ossClient = createOSSClient();
        try {
            // 从URL中提取对象名称
            String objectName = extractObjectNameFromUrl(fileUrl);
            if (StrUtil.isBlank(objectName)) {
                log.warn("无法从URL中提取对象名称: {}", fileUrl);
                return false;
            }

            // 删除文件
            ossClient.deleteObject(bucketName, objectName);
            log.info("文件删除成功: {}", fileUrl);
            return true;

        } catch (OSSException | ClientException e) {
            log.error("文件删除失败: {}", fileUrl, e);
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 批量删除文件
     * 
     * @param fileUrls 文件URL列表
     * @return 删除结果
     */
    public boolean deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            log.warn("文件URL列表为空，跳过删除");
            return false;
        }

        OSS ossClient = createOSSClient();
        try {
            // 提取对象名称列表
            List<String> objectNames = new ArrayList<>();
            for (String fileUrl : fileUrls) {
                String objectName = extractObjectNameFromUrl(fileUrl);
                if (StrUtil.isNotBlank(objectName)) {
                    objectNames.add(objectName);
                }
            }

            if (objectNames.isEmpty()) {
                log.warn("没有有效的对象名称，跳过删除");
                return false;
            }

            // 批量删除
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName);
            deleteObjectsRequest.setKeys(objectNames);

            DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteObjectsRequest);
            List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();

            log.info("批量文件删除成功，共{}个文件", deletedObjects.size());
            return true;

        } catch (OSSException | ClientException e) {
            log.error("批量文件删除失败", e);
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 从URL中提取对象名称
     */
    private String extractObjectNameFromUrl(String fileUrl) {
        try {
            if (StrUtil.isNotBlank(customDomain)) {
                // 使用自定义域名
                String prefix = (useHttps ? "https://" : "http://") + customDomain + "/";
                if (fileUrl.startsWith(prefix)) {
                    return fileUrl.substring(prefix.length());
                }
            } else {
                // 使用默认域名
                String httpsPrefix = "https://" + bucketName + "." + endPoint + "/";
                String httpPrefix = "http://" + bucketName + "." + endPoint + "/";

                if (fileUrl.startsWith(httpsPrefix)) {
                    return fileUrl.substring(httpsPrefix.length());
                } else if (fileUrl.startsWith(httpPrefix)) {
                    return fileUrl.substring(httpPrefix.length());
                }
            }

            log.warn("无法识别的文件URL格式: {}", fileUrl);
            return null;
        } catch (Exception e) {
            log.error("提取对象名称失败: {}", fileUrl, e);
            return null;
        }
    }

    /**
     * 检查文件是否存在
     * 
     * @param fileUrl 文件URL
     * @return 是否存在
     */
    public boolean fileExists(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return false;
        }

        String objectName = extractObjectNameFromUrl(fileUrl);
        if (StrUtil.isBlank(objectName)) {
            return false;
        }

        OSS ossClient = createOSSClient();
        try {
            return ossClient.doesObjectExist(bucketName, objectName);
        } catch (Exception e) {
            log.error("检查文件存在性失败: {}", fileUrl, e);
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 获取文件信息
     * 
     * @param fileUrl 文件URL
     * @return 文件信息
     */
    public ObjectMetadata getFileInfo(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return null;
        }

        String objectName = extractObjectNameFromUrl(fileUrl);
        if (StrUtil.isBlank(objectName)) {
            return null;
        }

        OSS ossClient = createOSSClient();
        try {
            return ossClient.getObjectMetadata(bucketName, objectName);
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", fileUrl, e);
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 生成预签名URL（用于临时访问私有文件）
     * 
     * @param fileUrl    文件URL
     * @param expiration 过期时间
     * @return 预签名URL
     */
    public String generatePresignedUrl(String fileUrl, Date expiration) {
        if (StrUtil.isBlank(fileUrl)) {
            return null;
        }

        String objectName = extractObjectNameFromUrl(fileUrl);
        if (StrUtil.isBlank(objectName)) {
            return null;
        }

        OSS ossClient = createOSSClient();
        try {
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            return url.toString();
        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", fileUrl, e);
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 上传字节数组
     * 
     * @param bytes    字节数组
     * @param fileName 文件名
     * @param category 分类目录
     * @return 文件访问URL
     */
    public String uploadBytes(byte[] bytes, String fileName, String category) {
        if (bytes == null || bytes.length == 0) {
            throw new ServiceException("文件内容不能为空");
        }

        if (StrUtil.isBlank(fileName)) {
            throw new ServiceException("文件名不能为空");
        }

        OSS ossClient = createOSSClient();
        try {
            // 生成文件路径
            String objectName = generateFilePath(fileName, category);

            // 创建输入流
            InputStream inputStream = new ByteArrayInputStream(bytes);

            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName, objectName, inputStream);

            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            putObjectRequest.setMetadata(metadata);

            // 上传文件
            ossClient.putObject(putObjectRequest);

            // 构建访问URL
            String fileUrl = buildFileUrl(objectName);

            log.info("字节数组上传成功: {} -> {}", fileName, fileUrl);
            return fileUrl;

        } catch (OSSException | ClientException e) {
            log.error("字节数组上传失败: {}", fileName, e);
            throw new ServiceException("文件上传失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
