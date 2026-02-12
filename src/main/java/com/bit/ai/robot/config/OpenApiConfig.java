package com.bit.ai.robot.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger 文档配置
 * 启动后访问：http://localhost:8080/swagger-ui/index.html 进行接口调试
 * API 文档 JSON：http://localhost:8080/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("小哈 AI 机器人 API")
                        .description("AI 对话、智能客服、聊天记录管理等接口文档与调试")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("小哈 AI 机器人团队")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .externalDocs(new ExternalDocumentation()
                        .description("项目文档")
                        .url("https://github.com/example/xiaoha-ai-robot"));
    }
}
