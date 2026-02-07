package com.overdue.common.exception;

/**
 * 文件上传异常类
 * 
 * @author Tea Mall Team
 */
public class FileUploadException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误详情
     */
    private Object errorDetails;

    public FileUploadException() {
        super();
    }

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileUploadException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public FileUploadException(String errorCode, String message, Object errorDetails) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }

    public FileUploadException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Object getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(Object errorDetails) {
        this.errorDetails = errorDetails;
    }

    // 常用的文件上传错误码
    public static class ErrorCodes {
        public static final String FILE_EMPTY = "FILE_EMPTY";
        public static final String FILE_TOO_LARGE = "FILE_TOO_LARGE";
        public static final String INVALID_FILE_TYPE = "INVALID_FILE_TYPE";
        public static final String UPLOAD_FAILED = "UPLOAD_FAILED";
        public static final String DELETE_FAILED = "DELETE_FAILED";
        public static final String FILE_NOT_FOUND = "FILE_NOT_FOUND";
        public static final String INVALID_FILE_NAME = "INVALID_FILE_NAME";
        public static final String OSS_CONNECTION_ERROR = "OSS_CONNECTION_ERROR";
        public static final String PERMISSION_DENIED = "PERMISSION_DENIED";
        public static final String QUOTA_EXCEEDED = "QUOTA_EXCEEDED";
    }

    // 便捷的静态方法
    public static FileUploadException fileEmpty() {
        return new FileUploadException(ErrorCodes.FILE_EMPTY, "文件不能为空");
    }

    public static FileUploadException fileTooLarge(long maxSize) {
        return new FileUploadException(ErrorCodes.FILE_TOO_LARGE, 
            "文件大小超过限制，最大允许" + (maxSize / 1024 / 1024) + "MB");
    }

    public static FileUploadException invalidFileType(String extension) {
        return new FileUploadException(ErrorCodes.INVALID_FILE_TYPE, 
            "不支持的文件类型: " + extension);
    }

    public static FileUploadException uploadFailed(String reason) {
        return new FileUploadException(ErrorCodes.UPLOAD_FAILED, 
            "文件上传失败: " + reason);
    }

    public static FileUploadException deleteFailed(String reason) {
        return new FileUploadException(ErrorCodes.DELETE_FAILED, 
            "文件删除失败: " + reason);
    }

    public static FileUploadException fileNotFound(String fileUrl) {
        return new FileUploadException(ErrorCodes.FILE_NOT_FOUND, 
            "文件不存在: " + fileUrl);
    }

    public static FileUploadException invalidFileName() {
        return new FileUploadException(ErrorCodes.INVALID_FILE_NAME, 
            "文件名不能为空或包含非法字符");
    }

    public static FileUploadException ossConnectionError(String reason) {
        return new FileUploadException(ErrorCodes.OSS_CONNECTION_ERROR, 
            "OSS连接错误: " + reason);
    }

    public static FileUploadException permissionDenied() {
        return new FileUploadException(ErrorCodes.PERMISSION_DENIED, 
            "没有文件操作权限");
    }

    public static FileUploadException quotaExceeded() {
        return new FileUploadException(ErrorCodes.QUOTA_EXCEEDED, 
            "存储配额已满");
    }
}