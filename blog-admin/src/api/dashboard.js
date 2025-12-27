import request from '../utils/request'

// 获取数据统计
export const getDashboardStats = () => {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

// 获取最近文章
export const getRecentArticles = () => {
  return request({
    url: '/dashboard/recent-articles',
    method: 'get'
  })
}

// 获取最近评论
export const getRecentComments = () => {
  return request({
    url: '/dashboard/recent-comments',
    method: 'get'
  })
}

// 获取访问趋势
export const getVisitTrend = () => {
  return request({
    url: '/dashboard/visit-trend',
    method: 'get'
  })
}

// 获取文章分类统计
export const getCategoryStats = () => {
  return request({
    url: '/dashboard/category-stats',
    method: 'get'
  })
}
