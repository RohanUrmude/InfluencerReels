package com.viralforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ViralForgeAIApplication {
    public static void main(String[] args) {
        SpringApplication.run(ViralForgeAIApplication.class, args);
    }
}
