package com.example.users.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import java.util.List;
import java.util.logging.Filter;
@Configuration
public class cors {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                       .allowedOrigins("http://localhost:4200","https://dev.beeconstruct.dpc.com.tn")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH" , "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);


               /* registry.addMapping("/users/verify-otp")
                        .allowedOrigins("http://localhost:4200","https://dev.beeconstruct.dpc.com.tn")
                        .allowedMethods("GET", "POST")
                        .allowedHeaders("*")
                        .allowCredentials(true); */
            }
        };
    }
}
