package com.panyu.mybolg.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.panyu.mybolg.common.Result;
import com.panyu.mybolg.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${weather.api.key:}")
    private String weatherApiKey;

    @GetMapping("/ip")
    public Result<Object> getIpLocation(@RequestParam(required = false) String ip) {
        try {
            // 构建API URL
            String apiUrl = "https://v2.xxapi.cn/api/ip";
            if (ip != null && !ip.isEmpty()) {
                apiUrl += "?ip=" + ip;
            } else {
                apiUrl += "?ip="; // 让API自动识别
            }

            // 发起请求
            String response = HttpClientUtil.get(apiUrl);
            // 将JSON字符串转换为Object
            Object responseObject = objectMapper.readValue(response, Object.class);
            return Result.success(responseObject);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取IP位置信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/data")
    public Result<Object> getWeather(@RequestParam String city) {
        try {
            // 检查是否配置了API密钥
            if (weatherApiKey == null || weatherApiKey.isEmpty()) {
                return Result.error("天气API密钥未配置，请在配置文件中设置weather.api.key");
            }
            
            // 构建API URL
            String apiUrl = String.format("https://v2.xxapi.cn/api/weather?city=%s&key=%s", city, weatherApiKey);

            // 发起请求
            String response = HttpClientUtil.get(apiUrl);
            // 将JSON字符串转换为Object
            Object responseObject = objectMapper.readValue(response, Object.class);
            return Result.success(responseObject);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取天气信息失败: " + e.getMessage());
        }
    }
}