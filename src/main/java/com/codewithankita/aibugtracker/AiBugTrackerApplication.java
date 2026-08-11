package com.codewithankita.aibugtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = {
        "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
})
public class AiBugTrackerApplication {

    public static void main(String[] args) {
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl != null && !dbUrl.isBlank()) {
            String cleanUrl = dbUrl.replaceAll("\\s+", "");
            System.setProperty("spring.datasource.url", cleanUrl);
        }
        SpringApplication.run(AiBugTrackerApplication.class, args);
    }

}
