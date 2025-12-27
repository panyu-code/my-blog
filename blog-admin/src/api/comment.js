import request from '../utils/request'

// 获取评论列表
export const getCommentList = (params) => {
  return request({
    url: '/comment/list',
    method: 'get',
    params
  })
}

// 根据文章获取评论
export const getArticleComments = (articleId, params) => {
  return request({
    url: `/comment/${articleId}/list`,
    method: 'get',
    params
  })
}

// 删除评论
export const deleteComment = (id) => {
  return request({
    url: `/comment/${id}`,
    method: 'delete'
  })
}

// 审核评论
export const auditComment = (id, status) => {
  return request({
    url: `/comment/${id}/audit`,
    method: 'put',
    data: { status }
  })
}
