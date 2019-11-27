package com.zhihui.zhihuijiazu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan(value = "com.zhihui.zhihuijiazu.Dao")
@EnableScheduling
@EnableTransactionManagement
public class ZhihuijiazuApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ZhihuijiazuApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ZhihuijiazuApplication.class);
    }



}
