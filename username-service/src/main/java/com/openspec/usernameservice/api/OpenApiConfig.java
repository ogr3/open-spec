package com.openspec.usernameservice.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI usernameServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Username Service API")
                        .version("1.0.0")
                        .description("API for generating Swedish-aware usernames"));
    }
}
