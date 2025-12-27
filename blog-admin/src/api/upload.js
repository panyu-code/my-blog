import request from '../utils/request'

// 获取上传文件列表
export const getUploadList = (params) => {
  return request({
    url: '/upload/list',
    method: 'get',
    params
  })
}

// 上传文件
export const uploadFile = (data) => {
  return request({
    url: '/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除文件
export const deleteUpload = (id) => {
  return request({
    url: `/upload/${id}`,
    method: 'delete'
  })
}
