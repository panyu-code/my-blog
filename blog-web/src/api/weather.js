import request from './request'

// 通过后端代理获取天气信息
export const getIpLocation = (ip) => {
  return request({
    url: '/weather/ip',
    method: 'get',
    params: {
      ip: ip
    }
  })
}

export const getWeather = (city) => {
  return request({
    url: '/weather/data',
    method: 'get',
    params: {
      city: city
    }
  })
}