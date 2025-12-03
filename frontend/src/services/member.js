import api from './api'

/**
 * 会员服务API
 */

/**
 * 获取会员信息
 */
export const getMemberInfo = () => {
  return api.get('/member/info')
}

/**
 * 获取成长值记录
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 */
export const getGrowthRecords = (page = 0, size = 10) => {
  return api.get('/member/growth-records', {
    params: { page, size }
  })
}

/**
 * 生成邀请链接
 */
export const generateInvitationLink = () => {
  return api.get('/member/invitation/generate')
}

/**
 * 查询邀请记录
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 */
export const getInvitationRecords = (page = 0, size = 10) => {
  return api.get('/member/invitation/records', {
    params: { page, size }
  })
}

/**
 * 会员等级常量
 */
export const MemberLevels = {
  ROOKIE: {
    code: 'ROOKIE',
    name: '新手会员',
    color: '#95a5a6',
    order: 1
  },
  BRONZE: {
    code: 'BRONZE',
    name: '青铜会员',
    color: '#cd7f32',
    order: 2
  },
  SILVER: {
    code: 'SILVER',
    name: '白银会员',
    color: '#c0c0c0',
    order: 3
  },
  GOLD: {
    code: 'GOLD',
    name: '黄金会员',
    color: '#ffd700',
    order: 4
  },
  PLATINUM: {
    code: 'PLATINUM',
    name: '铂金会员',
    color: '#e5e4e2',
    order: 5
  }
}

/**
 * 成长值类型
 */
export const GrowthTypes = {
  SIGN_IN: { name: '每日签到', color: '#67c23a' },
  FOOD_RECORD: { name: '记录饮食', color: '#409eff' },
  AI_CHAT: { name: 'AI咨询', color: '#e6a23c' },
  INVITATION: { name: '邀请好友', color: '#f56c6c' },
  LEVEL_UP: { name: '等级提升', color: '#909399' },
  SYSTEM_REWARD: { name: '系统奖励', color: '#ff69b4' }
}
