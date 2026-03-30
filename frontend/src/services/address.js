import api from './api'

/**
 * 收货地址管理服务
 */

/** 获取用户所有收货地址 */
export const getAddresses = () => api.get('/addresses')

/** 新增收货地址 */
export const addAddress = data => api.post('/addresses', data)

/** 更新收货地址 */
export const updateAddress = (id, data) => api.put(`/addresses/${id}`, data)

/** 删除收货地址 */
export const deleteAddress = id => api.delete(`/addresses/${id}`)

/** 设为默认地址 */
export const setDefaultAddress = id => api.put(`/addresses/${id}/default`)
