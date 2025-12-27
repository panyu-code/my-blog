import request from '../utils/request'

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

// 创建文章
export const createArticle = (data) => {
  return request({
    url: '/article',
    method: 'post',
    data
  })
}

// 更新文章
export const updateArticle = (data) => {
  return request({
    url: '/article',
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

// 获取分类列表
export const getCategoryList = () => {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

// 创建分类
export const createCategory = (data) => {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

// 更新分类
export const updateCategory = (data) => {
  return request({
    url: '/category',
    method: 'put',
    data
  })
}

// 删除分类
export const deleteCategory = (id) => {
  return request({
    url: `/category/${id}`,
    method: 'delete'
  })
}

// 获取标签列表
export const getTagList = () => {
  return request({
    url: '/tag/list',
    method: 'get'
  })
}

// 创建标签
export const createTag = (data) => {
  return request({
    url: '/tag',
    method: 'post',
    data
  })
}

// 更新标签
export const updateTag = (data) => {
  return request({
    url: '/tag',
    method: 'put',
    data
  })
}

// 删除标签
export const deleteTag = (id) => {
  return request({
    url: `/tag/${id}`,
    method: 'delete'
  })
}
