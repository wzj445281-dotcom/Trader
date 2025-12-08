package com.trader.app;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File; // 🔥 修复点：导入 java.io.File

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        // 使用 System.getProperty("user.dir") 获取应用的绝对运行目录
        // File.separator 确保路径分隔符在不同系统下正确（Windows 是 \, Linux/Mac 是 /）
        String uploadsPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

        // 确保使用 file: 前缀和正确的路径分隔符
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // 确保资源映射也使用 file: 绝对路径
                registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadsPath);
            }
        };
    }
}