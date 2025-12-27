import request from '../utils/request'

// 获取系统设置列表
export const getSettingsList = () => {
  return request({
    url: '/settings/list',
    method: 'get'
  })
}

// 获取单个设置
export const getSetting = (key) => {
  return request({
    url: `/settings/${key}`,
    method: 'get'
  })
}

// 更新设置
export const updateSettings = (data) => {
  return request({
    url: '/settings',
    method: 'put',
    data
  })
}
