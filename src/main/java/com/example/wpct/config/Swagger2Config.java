package com.example.wpct.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Author ZXX
 * @ClassName Swagger2Config
 * @Description 生成接口文档配置类  http://localhost:8080/swagger-ui.html
 * @DATE 2022/10/3 11:13
 */

@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket docket(){
        return new Docket(DocumentationType.SWAGGER_2).
                apiInfo(new ApiInfoBuilder().title("武平城投接口文档").build());
    }
}
