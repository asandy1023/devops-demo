package org.example.devops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author JQ
 */
@EnableDiscoveryClient
@SpringBootApplication
public class DevopsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevopsDemoApplication.class, args);
    }
}
