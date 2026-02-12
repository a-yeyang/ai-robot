package com.bit.ai.robot.model.vo.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * @author: sunny
 * @url: www.bit.com
 * @date: 2025/7/11 18:07
 * @description: 对话分页
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindChatHistoryPageListRspVO {
    /**
     * 对话 ID
     */
    private Long id;
    /**
     * 对话 UUID
     */
    private String uuid;
    /**
     * 对话摘要
     */
    private String summary;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
