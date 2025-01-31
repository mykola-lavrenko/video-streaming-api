package com.mlavrenko.videostreaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@ConfigurationPropertiesScan
public class VideoStreamingApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoStreamingApiApplication.class, args);
    }
}
