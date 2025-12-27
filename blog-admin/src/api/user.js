import request from '../utils/request'

// 用户登录
export const login = (data) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 获取用户列表
export const getUserList = (params) => {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

// 获取用户信息
export const getUserInfo = (id) => {
  return request({
    url: '/user/info',
    method: 'get',
    params: { id }
  })
}

// 创建用户
export const createUser = (data) => {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

// 更新用户
export const updateUser = (data) => {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

// 删除用户
export const deleteUser = (id) => {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

// 更新用户状态
export const updateUserStatus = (id, status) => {
  return request({
    url: `/user/${id}/status`,
    method: 'put',
    data: { status }
  })
}
