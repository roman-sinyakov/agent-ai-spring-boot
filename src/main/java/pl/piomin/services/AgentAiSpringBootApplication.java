package pl.piomin.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import pl.piomin.services.config.AppSecurityProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppSecurityProperties.class)
public class AgentAiSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentAiSpringBootApplication.class, args);
    }
}
