package com.treinotracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI treinoTrackerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Treino Tracker API")
                .description("API REST para acompanhamento de treinos (progressão semanal e 1RM estimado pela fórmula de Epley) e de hidratação diária.")
                .version("v1"));
    }
}
