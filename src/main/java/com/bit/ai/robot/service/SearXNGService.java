package com.bit.ai.robot.service;

import com.bit.ai.robot.model.dto.SearchResultDTO;

import java.util.List;

/**
 * @Author: sunny
 * @Date: 2025/7/13 21:00
 * @Version: v1.0.0
 * @Description: TODO
 **/
public interface SearXNGService {

    /**
     * 调用 SearXNG Api, 获取搜索列表
     * @param query 搜索关键词
     * @return
     */
    List<SearchResultDTO> search(String query);
}
