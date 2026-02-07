/**
 * 邀请相关工具函数
 */
import storage from './storage.js'

class InviteUtil {
  /**
   * 生成邀请码
   * @param {string} spaceId - 空间ID
   * @param {string} inviterId - 邀请人ID
   * @param {string} inviterName - 邀请人名称
   * @param {number} expireDays - 有效期天数，默认7天
   * @returns {string} 邀请码
   */
  generateInviteCode(spaceId, inviterId, inviterName, expireDays = 7) {
    // 生成唯一邀请码
    const timestamp = Date.now()
    const random = Math.random().toString(36).substring(2, 8).toUpperCase()
    const inviteCode = `INV${timestamp.toString(36).toUpperCase()}${random}`
    
    // 计算过期时间
    const expireTime = new Date()
    expireTime.setDate(expireTime.getDate() + expireDays)
    
    // 保存邀请信息
    const invites = storage.get('invites', [])
    invites.push({
      inviteCode,
      spaceId,
      inviterId,
      inviterName,
      expireTime: expireTime.toISOString(),
      createdAt: new Date().toISOString(),
      used: false
    })
    
    storage.set('invites', invites)
    
    return inviteCode
  }

  /**
   * 获取邀请信息
   * @param {string} inviteCode - 邀请码
   * @returns {Object|null} 邀请信息
   */
  getInviteInfo(inviteCode) {
    const invites = storage.get('invites', [])
    return invites.find(inv => inv.inviteCode === inviteCode && !inv.used) || null
  }

  /**
   * 验证邀请码是否有效
   * @param {string} inviteCode - 邀请码
   * @returns {boolean} 是否有效
   */
  validateInviteCode(inviteCode) {
    const inviteInfo = this.getInviteInfo(inviteCode)
    if (!inviteInfo) {
      return false
    }
    
    // 检查是否过期
    const now = new Date()
    const expireTime = new Date(inviteInfo.expireTime)
    if (now > expireTime) {
      return false
    }
    
    return true
  }

  /**
   * 标记邀请码为已使用
   * @param {string} inviteCode - 邀请码
   */
  markInviteAsUsed(inviteCode) {
    const invites = storage.get('invites', [])
    const index = invites.findIndex(inv => inv.inviteCode === inviteCode)
    if (index !== -1) {
      invites[index].used = true
      invites[index].usedAt = new Date().toISOString()
      storage.set('invites', invites)
    }
  }

  /**
   * 移除邀请码（已过期或已使用）
   * @param {string} inviteCode - 邀请码
   */
  removeInvite(inviteCode) {
    const invites = storage.get('invites', [])
    const filteredInvites = invites.filter(inv => inv.inviteCode !== inviteCode)
    storage.set('invites', filteredInvites)
  }

  /**
   * 获取空间的邀请列表
   * @param {string} spaceId - 空间ID
   * @returns {Array} 邀请列表
   */
  getSpaceInvites(spaceId) {
    const invites = storage.get('invites', [])
    return invites.filter(inv => inv.spaceId === spaceId)
  }

  /**
   * 清理过期的邀请码
   */
  cleanExpiredInvites() {
    const invites = storage.get('invites', [])
    const now = new Date()
    const validInvites = invites.filter(inv => {
      const expireTime = new Date(inv.expireTime)
      return expireTime > now && !inv.used
    })
    storage.set('invites', validInvites)
  }

  /**
   * 生成邀请链接
   * @param {string} inviteCode - 邀请码
   * @returns {string} 邀请链接
   */
  generateInviteLink(inviteCode) {
    // TODO: 实际项目中应该使用真实的域名
    // 在uni-app中，可以使用小程序码或H5链接
    // 这里返回一个格式化的链接，实际使用时需要替换为真实域名
    return `https://your-app.com/invite/${inviteCode}`
    
    // 如果是小程序，可以使用以下方式生成小程序码
    // return `/pages/shared/join?inviteCode=${inviteCode}`
  }

  /**
   * 生成二维码内容（用于生成二维码）
   * @param {string} inviteCode - 邀请码
   * @returns {string} 二维码内容
   */
  generateQRCodeContent(inviteCode) {
    // 返回邀请链接，用于生成二维码
    return this.generateInviteLink(inviteCode)
  }
}

export default new InviteUtil()
