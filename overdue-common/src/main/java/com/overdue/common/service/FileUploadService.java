package com.overdue.common.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.overdue.common.config.OssConfig;
import com.overdue.common.core.domain.dto.FileUploadResult;
import com.overdue.common.exception.ServiceException;
import com.overdue.common.utils.OssUtils;
import com.overdue.common.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 文件上传服务
 * 提供更丰富的文件上传功能和信息
 * 
 * @author Tea Mall Team
 */
@Slf4j
@Service
public class FileUploadService {

    @Autowired
    private OssUtils ossUtils;

    @Autowired
    private OssConfig ossConfig;

    /**
     * 上传单个文件并返回详细信息
     * 
     * @param file 上传的文件
     * @param category 文件分类
     * @return 文件上传结果
     */
    public FileUploadResult uploadFileWithDetails(MultipartFile file, String category) {
        try {
            // 验证文件
            validateFile(file);
            
            // 计算文件MD5
            String md5 = DigestUtil.md5Hex(file.getInputStream());
            
            // 上传文件
            String fileUrl = ossUtils.uploadOneFile(file, category);
            
            // 构建结果
            FileUploadResult result = FileUploadResult.builder()
                .url(fileUrl)
                .originalFileName(file.getOriginalFilename())
                .fileName(extractFileNameFromUrl(fileUrl))
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .extension(getFileExtension(file.getOriginalFilename()))
                .category(category)
                .uploadTime(new Date())
                .md5(md5)
                .build();
            
            // 如果是图片，获取图片尺寸
            if (isImageFile(file)) {
                try {
                    BufferedImage image = ImageIO.read(file.getInputStream());
                    if (image != null) {
                        result.setIsImage(true);
                        result.setWidth(image.getWidth());
                        result.setHeight(image.getHeight());
                    }
                } catch (IOException e) {
                    log.warn("获取图片尺寸失败: {}", file.getOriginalFilename(), e);
                }
            } else {
                result.setIsImage(false);
            }
            
            LogUtils.logFileUpload(log, file.getOriginalFilename(), file.getSize(), "SUCCESS", 
                System.currentTimeMillis() - System.currentTimeMillis());
            return result;
            
        } catch (IOException e) {
            LogUtils.logError(log, "FILE_UPLOAD", "文件上传失败: " + file.getOriginalFilename(), e);
            throw new ServiceException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件并返回详细信息
     * 
     * @param files 上传的文件数组
     * @param category 文件分类
     * @return 文件上传结果列表
     */
    public List<FileUploadResult> uploadFilesWithDetails(MultipartFile[] files, String category) {
        if (files == null || files.length == 0) {
            throw new ServiceException("文件列表不能为空");
        }

        List<FileUploadResult> results = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                FileUploadResult result = uploadFileWithDetails(file, category);
                results.add(result);
            } catch (Exception e) {
                log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                failedFiles.add(file.getOriginalFilename());
            }
        }

        if (!failedFiles.isEmpty()) {
            log.warn("部分文件上传失败: {}", failedFiles);
            throw new ServiceException("部分文件上传失败: " + String.join(", ", failedFiles));
        }

        log.info("批量文件上传成功，共{}个文件", results.size());
        return results;
    }

    /**
     * 上传图片文件
     * 
     * @param file 图片文件
     * @return 文件上传结果
     */
    public FileUploadResult uploadImage(MultipartFile file) {
        if (!isImageFile(file)) {
            throw new ServiceException("只能上传图片文件");
        }
        return uploadFileWithDetails(file, "images");
    }

    /**
     * 上传文档文件
     * 
     * @param file 文档文件
     * @return 文件上传结果
     */
    public FileUploadResult uploadDocument(MultipartFile file) {
        if (!isDocumentFile(file)) {
            throw new ServiceException("只能上传文档文件");
        }
        return uploadFileWithDetails(file, "documents");
    }

    /**
     * 上传视频文件
     * 
     * @param file 视频文件
     * @return 文件上传结果
     */
    public FileUploadResult uploadVideo(MultipartFile file) {
        if (!isVideoFile(file)) {
            throw new ServiceException("只能上传视频文件");
        }
        return uploadFileWithDetails(file, "videos");
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("文件不能为空");
        }

        if (file.getSize() > ossConfig.getMaxFileSize()) {
            throw new ServiceException("文件大小不能超过" + (ossConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new ServiceException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!isAllowedExtension(extension)) {
            throw new ServiceException("不支持的文件格式: " + extension);
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
        return isImageExtension(extension) || 
               isVideoExtension(extension) || 
               isDocumentExtension(extension);
    }

    /**
     * 检查是否为图片扩展名
     */
    private boolean isImageExtension(String extension) {
        return Arrays.asList(ossConfig.getAllowedImageExtensions()).contains(extension.toLowerCase());
    }

    /**
     * 检查是否为视频扩展名
     */
    private boolean isVideoExtension(String extension) {
        return Arrays.asList(ossConfig.getAllowedVideoExtensions()).contains(extension.toLowerCase());
    }

    /**
     * 检查是否为文档扩展名
     */
    private boolean isDocumentExtension(String extension) {
        return Arrays.asList(ossConfig.getAllowedDocumentExtensions()).contains(extension.toLowerCase());
    }

    /**
     * 检查是否为图片文件
     */
    private boolean isImageFile(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        return isImageExtension(extension);
    }

    /**
     * 检查是否为视频文件
     */
    private boolean isVideoFile(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        return isVideoExtension(extension);
    }

    /**
     * 检查是否为文档文件
     */
    private boolean isDocumentFile(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        return isDocumentExtension(extension);
    }

    /**
     * 从URL中提取文件名
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return "";
        }
        int lastSlashIndex = fileUrl.lastIndexOf("/");
        return lastSlashIndex > 0 ? fileUrl.substring(lastSlashIndex + 1) : fileUrl;
    }
}