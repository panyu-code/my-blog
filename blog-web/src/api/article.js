import request from './request'

// 获取文章列表
export const getArticleList = (params) => {
  return request({
    url: '/article/list',
    method: 'get',
    params
  })
}

// 获取文章详情
export const getArticleDetail = (id) => {
  return request({
    url: `/article/${id}`,
    method: 'get'
  })
}

// 搜索文章
export const searchArticles = (keyword) => {
  return request({
    url: '/article/search',
    method: 'get',
    params: { keyword }
  })
}

// 获取分类列表
export const getCategoryList = () => {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

// 根据分类获取文章
export const getArticlesByCategory = (categoryId, params) => {
  return request({
    url: `/category/${categoryId}/articles`,
    method: 'get',
    params
  })
}

// 获取标签列表
export const getTagList = () => {
  return request({
    url: '/tag/list',
    method: 'get'
  })
}

// 根据标签获取文章
export const getArticlesByTag = (tagId, params) => {
  return request({
    url: `/tag/${tagId}/articles`,
    method: 'get',
    params
  })
}

// 文章点赞
export const likeArticle = (id) => {
  return request({
    url: `/article/${id}/like`,
    method: 'post'
  })
}

// 增加文章浏讯量
export const increaseView = (id) => {
  return request({
    url: `/article/${id}/view`,
    method: 'post'
  })
}

// 发布文章
export const createArticle = (data) => {
  return request({
    url: '/article',
    method: 'post',
    data
  })
}

// 更新文章（管理端）
export const updateArticle = (data) => {
  return request({
    url: '/article',
    method: 'put',
    data
  })
}

// 更新自己的文章（用户端）
export const updateUserArticle = (data) => {
  return request({
    url: '/article/user',
    method: 'put',
    data
  })
}

// 删除文章
export const deleteArticle = (id) => {
  return request({
    url: `/article/${id}`,
    method: 'delete'
  })
}

// 获取用户文章列表
export const getUserArticles = (params) => {
  return request({
    url: '/article/user/list',
    method: 'get',
    params
  })
}

// 重新提交审核
export const resubmitArticle = (id) => {
  return request({
    url: `/article/${id}/resubmit`,
    method: 'post'
  })
}

// 获取用户自己的文章详情
export const getUserArticleDetail = (id) => {
  return request({
    url: `/article/user/${id}`,
    method: 'get'
  })
}
