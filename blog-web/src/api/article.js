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

// 增加文章浏览量
export const increaseView = (id) => {
  return request({
    url: `/article/${id}/view`,
    method: 'post'
  })
}
