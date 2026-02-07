package com.overdue.common.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件上传结果DTO
 * 
 * @author Tea Mall Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件访问URL
     */
    private String url;

    /**
     * 原始文件名
     */
    private String originalFileName;

    /**
     * 存储文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 文件扩展名
     */
    private String extension;

    /**
     * 文件分类
     */
    private String category;

    /**
     * 上传时间
     */
    private Date uploadTime;

    /**
     * 文件MD5值
     */
    private String md5;

    /**
     * 是否为图片
     */
    private Boolean isImage;

    /**
     * 图片宽度（仅图片文件）
     */
    private Integer width;

    /**
     * 图片高度（仅图片文件）
     */
    private Integer height;
}