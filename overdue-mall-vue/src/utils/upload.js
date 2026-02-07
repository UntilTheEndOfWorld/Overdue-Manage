/**
 * 文件上传URL管理工具
 * 根据环境动态生成上传URL
 */

/**
 * 获取OSS上传URL
 * @returns {string} 上传URL
 */
export function getOssUploadUrl() {
  if (process.env.NODE_ENV === 'production') {
    return "/jx_slr_upload/oss/upload";
  } else {
    return "/api/oss/upload";
  }
}

/**
 * 获取通用上传URL
 * @returns {string} 上传URL
 */
export function getCommonUploadUrl() {
  if (process.env.NODE_ENV === 'production') {
    return "/jx_slr_upload/common/upload";
  } else {
    return "/api/common/upload";
  }
}

/**
 * 获取H5文件上传URL
 * @param {string} type 上传类型 (image/document/video/smart)
 * @returns {string} 上传URL
 */
export function getH5UploadUrl(type = 'image') {
  if (process.env.NODE_ENV === 'production') {
    return `/jx_slr_upload/h5/file/upload-${type}`;
  } else {
    return `/api/h5/file/upload-${type}`;
  }
}

/**
 * 获取完整的上传URL（包含域名）
 * @param {string} path 路径
 * @returns {string} 完整URL
 */
export function getFullUploadUrl(path) {
  if (process.env.NODE_ENV === 'production') {
    return `https://www.lycc.ltd${path}`;
  } else {
    return `http://localhost:8099${path}`;
  }
}

export default {
  getOssUploadUrl,
  getCommonUploadUrl,
  getH5UploadUrl,
  getFullUploadUrl
}
