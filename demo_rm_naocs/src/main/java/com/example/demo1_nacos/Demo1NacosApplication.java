package com.example.demo1_nacos;

import cn.amberdata.common.response.annotation.EnableGlobalResponseBody;
import cn.amberdata.dm.common.mq.config.RocketMqConsumerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Description:
 *
 * @author Created by xutingrong
 * @since 2022/02/10
 */
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@EnableGlobalResponseBody
@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
@ComponentScan(basePackages = {      "cn.amberdata.afc", "cn.amberdata.dm", "cn.amberdata.rm", "cn.amberdata.tdr", "cn.amberdata.common.mq", "cn.amberdata.validator",
        "com.example.demo1_nacos.mapper.tdr",
        "com.example.demo1_nacos.controller",
        "com.example.demo1_nacos.service",
        "com.example.demo1_nacos.collector",
        "com.example.demo1_nacos.dbconfig"})
public class Demo1NacosApplication {

    private static final Logger StartLogger = LoggerFactory.getLogger(Demo1NacosApplication.class);

    public static void main(String[] args) {
        try {
            SpringApplication app = new SpringApplication(Demo1NacosApplication.class);
            app.run(args);
            StartLogger.info("TdrApplication App Start !!!!!!");
        } catch (Exception e) {
            StartLogger.error("TdrApplication App Start Failed !!!!!!!!", e);
            throw e;
        }
    }

}
