package cn.sdh.travel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智能旅游规划助手 - 启动类
 * 基于Spring AI Alibaba的多Agent监督者模式实现
 */
@SpringBootApplication
public class TravelAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelAssistantApplication.class, args);
    }
}
