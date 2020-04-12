package com.redis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Lenovo
 * @date 2020-04-10 23:15
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.redis.mapper"})
// 扫描所有包以及相关组件包
public class RedisApplication {
    public static void main(String[] args)
    {
        SpringApplication.run(RedisApplication.class, args);
        System.out.println("启动成功");
    }
}
