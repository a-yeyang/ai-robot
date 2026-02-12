package com.bit.ai.robot.controller;

import com.bit.ai.robot.model.vo.customerService.*;
import com.google.common.collect.Lists;
import com.bit.ai.robot.advisor.CustomerServiceAdvisor;
import com.bit.ai.robot.aspect.ApiOperationLog;
import com.bit.ai.robot.service.CustomerService;
import com.bit.ai.robot.utils.PageResponse;
import com.bit.ai.robot.utils.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @Author: sunny
 * @Date: 2025/7/10 18:30
 * @Version: v1.0.0
 * @Description: AI 客服
 **/
@Tag(name = "AI 智能客服", description = "AI 智能客服相关接口")
@RestController
@RequestMapping("/customer-service")
@Slf4j
public class AiCustomerServiceController {

    @Resource
    private CustomerService customerService;
    @Resource
    private VectorStore vectorStore;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    @Value("${customer-service.model}")
    private String model;
    @Value("${customer-service.temperature}")
    private Double temperature;

    /**
     * 问答 MD 文件上传
     * @param file
     * @return
     */
    @Operation(summary = "上传 Markdown 文件", description = "上传 Markdown 格式的问答知识库文件")
    @PostMapping(value = "/md/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<?> uploadMarkdownFile(@RequestPart(value = "file", required = false) MultipartFile file) {
        return customerService.uploadMarkdownFile(file);
    }

    @Operation(summary = "删除 Markdown 文件", description = "删除已上传的 Markdown 问答文件")
    @PostMapping("/md/delete")
    @ApiOperationLog(description = "删除 Markdown 问答文件")
    public Response<?> deleteMarkdownFile(@RequestBody @Validated DeleteMarkdownFileReqVO deleteMarkdownFileReqVO) {
        return customerService.deleteMarkdownFile(deleteMarkdownFileReqVO);
    }

    @Operation(summary = "查询 Markdown 文件列表", description = "分页查询已上传的 Markdown 问答文件列表")
    @PostMapping("/md/list")
    @ApiOperationLog(description = "Markdown 问答文件分页查询")
    public PageResponse<FindMarkdownFilePageListRspVO> findMarkdownFilePageList(@RequestBody @Validated FindMarkdownFilePageListReqVO findMarkdownFilePageListReqVO) {
        return customerService.findMarkdownFilePageList(findMarkdownFilePageListReqVO);
    }

    @PostMapping("/md/update")
    @ApiOperationLog(description = "删除 Markdown 问答文件")
    public Response<?> updateMarkdownFile(@RequestBody @Validated UpdateMarkdownFileReqVO updateMarkdownFileReqVO) {
        return customerService.updateMarkdownFile(updateMarkdownFileReqVO);
    }

    /**
     * 流式对话
     * @return
     */
    @GetMapping(value = "/chat/completion", produces = "text/html;charset=utf-8")
    @ApiOperationLog(description = "AI 智能客服对话")
    public Flux<String> chat(@RequestParam(value = "message") String userMessage) {
        // 构建 ChatModel
        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(baseUrl)
                        .apiKey(apiKey)
                        .build())
                .build();

        // 动态设置调用的模型名称、温度值
        ChatClient.ChatClientRequestSpec chatClientRequestSpec = ChatClient.create(chatModel)
                .prompt()
                .options(OpenAiChatOptions.builder()
                        .model(model)
                        .temperature(temperature)
                        .build())
                .user(userMessage); // 用户提示词

        // Advisor 集合
        List<Advisor> advisors = Lists.newArrayList();
        advisors.add(new CustomerServiceAdvisor(vectorStore)); // 检索向量库，组合增强提示词

        // 应用 Advisor 集合
        chatClientRequestSpec.advisors(advisors);

        // 流式输出
        return chatClientRequestSpec
                .stream()
                .content();
    }

}
