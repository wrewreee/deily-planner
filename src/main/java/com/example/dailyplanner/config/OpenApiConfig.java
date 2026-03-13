package com.example.dailyplanner.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dailyPlannerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Daily Planner API")
                        .description("REST API для управления задачами в приложени 'Ежедневник' с использованием ИИ")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("VV")
                                .email("VV@gmail.com"))
                        .license(new License()
                                .name("Educational Use")));
    }
}
