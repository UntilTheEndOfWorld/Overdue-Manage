package com.overdue.common.manager;

import cn.hutool.core.util.StrUtil;
import com.overdue.common.config.OssConfig;
import com.overdue.common.core.domain.dto.FileUploadResult;
import com.overdue.common.exception.FileUploadException;
import com.overdue.common.service.FileUploadService;
import com.overdue.common.utils.OssUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 文件上传管理器
 * 提供高级的文件上传管理功能
 * 
 * @author Tea Mall Team
 */
@Slf4j
@Component
public class FileUploadManager {

    @Autowired
    private OssUtils ossUtils;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private OssConfig ossConfig;

    // 异步上传线程池
    private final Executor uploadExecutor = Executors.newFixedThreadPool(5);

    /**
     * 异步上传文件
     * 
     * @param file 上传的文件
     * @param category 文件分类
     * @return 异步上传结果
     */
    public CompletableFuture<FileUploadResult> uploadFileAsync(MultipartFile file, String category) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fileUploadService.uploadFileWithDetails(file, category);
            } catch (Exception e) {
                log.error("异步文件上传失败: {}", file.getOriginalFilename(), e);
                throw new FileUploadException("异步上传失败: " + e.getMessage(), e);
            }
        }, uploadExecutor);
    }

    /**
     * 异步批量上传文件
     * 
     * @param files 上传的文件数组
     * @param category 文件分类
     * @return 异步上传结果列表
     */
    public CompletableFuture<List<FileUploadResult>> uploadFilesAsync(MultipartFile[] files, String category) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fileUploadService.uploadFilesWithDetails(files, category);
            } catch (Exception e) {
                log.error("异步批量文件上传失败", e);
                throw new FileUploadException("异步批量上传失败: " + e.getMessage(), e);
            }
        }, uploadExecutor);
    }

    /**
     * 智能上传文件（根据文件类型自动分类）
     * 
     * @param file 上传的文件
     * @return 上传结果
     */
    public FileUploadResult smartUpload(MultipartFile file) {
        String category = determineCategory(file);
        return fileUploadService.uploadFileWithDetails(file, category);
    }

    /**
     * 根据文件类型确定分类
     * 
     * @param file 文件
     * @return 分类名称
     */
    private String determineCategory(MultipartFile file) {
        if (file == null || StrUtil.isBlank(file.getOriginalFilename())) {
            return "others";
        }

        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        
        // 图片文件
        if (isImageExtension(extension)) {
            return "images";
        }
        
        // 视频文件
        if (isVideoExtension(extension)) {
            return "videos";
        }
        
        // 文档文件
        if (isDocumentExtension(extension)) {
            return "documents";
        }
        
        // 音频文件
        if (isAudioExtension(extension)) {
            return "audios";
        }
        
        return "others";
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
     * 检查是否为图片扩展名
     */
    private boolean isImageExtension(String extension) {
        String[] imageExtensions = ossConfig.getAllowedImageExtensions();
        for (String ext : imageExtensions) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否为视频扩展名
     */
    private boolean isVideoExtension(String extension) {
        String[] videoExtensions = ossConfig.getAllowedVideoExtensions();
        for (String ext : videoExtensions) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否为文档扩展名
     */
    private boolean isDocumentExtension(String extension) {
        String[] documentExtensions = ossConfig.getAllowedDocumentExtensions();
        for (String ext : documentExtensions) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否为音频扩展名
     */
    private boolean isAudioExtension(String extension) {
        String[] audioExtensions = {"mp3", "wav", "flac", "aac", "ogg", "wma"};
        for (String ext : audioExtensions) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 批量删除文件（异步）
     * 
     * @param fileUrls 文件URL列表
     * @return 异步删除结果
     */
    public CompletableFuture<Boolean> deleteFilesAsync(List<String> fileUrls) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return ossUtils.deleteFiles(fileUrls);
            } catch (Exception e) {
                log.error("异步批量删除文件失败", e);
                throw new FileUploadException("异步删除失败: " + e.getMessage(), e);
            }
        }, uploadExecutor);
    }

    /**
     * 清理过期文件
     * 
     * @param days 保留天数
     * @return 清理结果
     */
    public CompletableFuture<Integer> cleanupExpiredFiles(int days) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 这里可以实现清理逻辑
                // 例如：查询数据库中超过指定天数的文件记录，然后删除
                log.info("开始清理{}天前的过期文件", days);
                
                // TODO: 实现具体的清理逻辑
                // 1. 查询过期文件列表
                // 2. 批量删除OSS文件
                // 3. 删除数据库记录
                
                int cleanedCount = 0;
                log.info("清理完成，共清理{}个过期文件", cleanedCount);
                return cleanedCount;
            } catch (Exception e) {
                log.error("清理过期文件失败", e);
                throw new FileUploadException("清理过期文件失败: " + e.getMessage(), e);
            }
        }, uploadExecutor);
    }

    /**
     * 获取存储统计信息
     * 
     * @return 存储统计
     */
    public StorageStats getStorageStats() {
        try {
            // TODO: 实现存储统计逻辑
            // 1. 统计各类型文件数量
            // 2. 统计存储空间使用情况
            // 3. 统计上传下载流量
            
            return StorageStats.builder()
                .totalFiles(0L)
                .totalSize(0L)
                .imageCount(0L)
                .videoCount(0L)
                .documentCount(0L)
                .build();
        } catch (Exception e) {
            log.error("获取存储统计失败", e);
            throw new FileUploadException("获取存储统计失败: " + e.getMessage(), e);
        }
    }

    /**
     * 存储统计信息
     */
    public static class StorageStats {
        private Long totalFiles;
        private Long totalSize;
        private Long imageCount;
        private Long videoCount;
        private Long documentCount;
        private Long audioCount;
        private Long otherCount;

        public static StorageStatsBuilder builder() {
            return new StorageStatsBuilder();
        }

        public static class StorageStatsBuilder {
            private Long totalFiles;
            private Long totalSize;
            private Long imageCount;
            private Long videoCount;
            private Long documentCount;
            private Long audioCount;
            private Long otherCount;

            public StorageStatsBuilder totalFiles(Long totalFiles) {
                this.totalFiles = totalFiles;
                return this;
            }

            public StorageStatsBuilder totalSize(Long totalSize) {
                this.totalSize = totalSize;
                return this;
            }

            public StorageStatsBuilder imageCount(Long imageCount) {
                this.imageCount = imageCount;
                return this;
            }

            public StorageStatsBuilder videoCount(Long videoCount) {
                this.videoCount = videoCount;
                return this;
            }

            public StorageStatsBuilder documentCount(Long documentCount) {
                this.documentCount = documentCount;
                return this;
            }

            public StorageStatsBuilder audioCount(Long audioCount) {
                this.audioCount = audioCount;
                return this;
            }

            public StorageStatsBuilder otherCount(Long otherCount) {
                this.otherCount = otherCount;
                return this;
            }

            public StorageStats build() {
                StorageStats stats = new StorageStats();
                stats.totalFiles = this.totalFiles;
                stats.totalSize = this.totalSize;
                stats.imageCount = this.imageCount;
                stats.videoCount = this.videoCount;
                stats.documentCount = this.documentCount;
                stats.audioCount = this.audioCount;
                stats.otherCount = this.otherCount;
                return stats;
            }
        }

        // Getters and Setters
        public Long getTotalFiles() { return totalFiles; }
        public void setTotalFiles(Long totalFiles) { this.totalFiles = totalFiles; }
        
        public Long getTotalSize() { return totalSize; }
        public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }
        
        public Long getImageCount() { return imageCount; }
        public void setImageCount(Long imageCount) { this.imageCount = imageCount; }
        
        public Long getVideoCount() { return videoCount; }
        public void setVideoCount(Long videoCount) { this.videoCount = videoCount; }
        
        public Long getDocumentCount() { return documentCount; }
        public void setDocumentCount(Long documentCount) { this.documentCount = documentCount; }
        
        public Long getAudioCount() { return audioCount; }
        public void setAudioCount(Long audioCount) { this.audioCount = audioCount; }
        
        public Long getOtherCount() { return otherCount; }
        public void setOtherCount(Long otherCount) { this.otherCount = otherCount; }
    }
}