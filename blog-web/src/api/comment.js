import request from './request'

// 获取评论列表
export const getCommentList = (articleId, params) => {
  return request({
    url: `/comment/${articleId}/list`,
    method: 'get',
    params
  })
}

// 添加评论
export const addComment = (data) => {
  return request({
    url: '/comment',
    method: 'post',
    data
  })
}

// 删除评论
export const deleteComment = (id) => {
  return request({
    url: `/comment/${id}`,
    method: 'delete'
  })
}

// 点赞评论
export const likeComment = (id) => {
  return request({
    url: `/comment/${id}/like`,
    method: 'post'
  })
}

// 回复评论
export const replyComment = (data) => {
  return request({
    url: '/comment/reply',
    method: 'post',
    data
  })
}
