package com.bit.ai.robot.advisor;

import com.bit.ai.robot.domain.dos.ChatMessageDO;
import com.bit.ai.robot.domain.mapper.ChatMessageMapper;
import com.bit.ai.robot.model.vo.customerService.AiCustomerServiceChatReqVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: sunny
 * @Date: 2026/2/14
 * @Version: v1.0.0
 * @Description: 智能客服流式对话日志和数据库保存 Advisor
 **/
@Slf4j
public class CustomerServiceStreamAdvisor implements StreamAdvisor {

    private final ChatMessageMapper chatMessageMapper;
    private final AiCustomerServiceChatReqVO aiCustomerServiceChatReqVO;
    private final TransactionTemplate transactionTemplate;

    public CustomerServiceStreamAdvisor(ChatMessageMapper chatMessageMapper,
                                       AiCustomerServiceChatReqVO aiCustomerServiceChatReqVO,
                                       TransactionTemplate transactionTemplate) {
        this.chatMessageMapper = chatMessageMapper;
        this.aiCustomerServiceChatReqVO = aiCustomerServiceChatReqVO;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public int getOrder() {
        return 99; // order 值越大，越晚执行
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        // 对话 UUID
        String chatUuid = aiCustomerServiceChatReqVO.getChatId();
        // 用户消息
        String userMessage = aiCustomerServiceChatReqVO.getMessage();

        // 流式调用
        Flux<ChatClientResponse> chatClientResponseFlux = streamAdvisorChain.nextStream(chatClientRequest);

        // 创建 AI 流式回答聚合容器（线程安全）
        AtomicReference<StringBuilder> fullContent = new AtomicReference<>(new StringBuilder());

        // 返回处理后的流
        return chatClientResponseFlux
                .doOnNext(response -> {
                    // 逐块收集内容
                    String chunk = response.chatResponse().getResult().getOutput().getText();

                    log.info("## [客服] chunk: {}", chunk);

                    // 若 chunk 块不为空，则追加到 fullContent 中
                    if (chunk != null) {
                        fullContent.get().append(chunk);
                    }
                })
                .doOnComplete(() -> {
                    // 流完成后打印完整回答
                    String completeResponse = fullContent.get().toString();
                    log.info("\n==== [客服] FULL AI RESPONSE ====\n{}\n========================", completeResponse);

                    // 如果没有传 chatId，则不保存到数据库
                    if (chatUuid == null || chatUuid.isEmpty()) {
                        log.info("## [客服] chatId 为空，不保存到数据库");
                        return;
                    }

                    // 开启编程式事务
                    transactionTemplate.execute(status -> {
                        try {
                            // 1. 存储用户消息
                            chatMessageMapper.insert(ChatMessageDO.builder()
                                    .chatUuid(chatUuid)
                                    .content(userMessage)
                                    .role(MessageType.USER.getValue()) // 用户消息
                                    .createTime(LocalDateTime.now())
                                    .build());

                            // 2. 存储 AI 回答
                            chatMessageMapper.insert(ChatMessageDO.builder()
                                    .chatUuid(chatUuid)
                                    .content(completeResponse)
                                    .role(MessageType.ASSISTANT.getValue()) // AI 回答
                                    .createTime(LocalDateTime.now())
                                    .build());

                            log.info("## [客服] 对话消息已保存到数据库, chatId: {}", chatUuid);
                            return true;
                        } catch (Exception ex) {
                            status.setRollbackOnly(); // 标记事务为回滚
                            log.error("## [客服] 保存对话消息失败", ex);
                        }
                        return false;
                    });
                })
                .doOnError(error -> {
                    // 出错时打印已收集的部分
                    String partialResponse = fullContent.get().toString();
                    log.error("## [客服] Stream 流出现错误，已收集回答如下: {}", partialResponse, error);
                });
    }
}
