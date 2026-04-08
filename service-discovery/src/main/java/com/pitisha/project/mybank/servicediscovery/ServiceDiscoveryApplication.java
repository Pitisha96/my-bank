package com.pitisha.project.mybank.servicediscovery;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableEurekaServer
public class ServiceDiscoveryApplication {

    public static void main(final String[] args) {
        run(ServiceDiscoveryApplication.class, args);
    }

}
