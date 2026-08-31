package com.acorn.gymmanagement;

import com.acorn.gymmanagement.payment.gateway.toss.TossPaymentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;


@SpringBootApplication(
        exclude = UserDetailsServiceAutoConfiguration.class
)
@EnableConfigurationProperties(TossPaymentProperties.class)
public class GymManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                GymManagementApplication.class,
                args
        );
    }
}
