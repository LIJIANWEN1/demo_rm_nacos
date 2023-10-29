package com.example.demo1_nacos;

import cn.amberdata.dm.common.mq.config.RocketMqConsumerConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@ComponentScan(excludeFilters  = {@ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = {RocketMqConsumerConfiguration.class})}, basePackages = {        "cn.amberdata.afc","cn.amberdata.dm","cn.amberdata.rm","cn.amberdata.cache","cn.amberdata.common",
        "com.example.demo1_nacos.mapper.tdr",
        "com.example.demo1_nacos.controller",
        "com.example.demo1_nacos.service",
        "com.example.demo1_nacos.collector",
        "com.example.demo1_nacos.dbconfig"})
//@MapperScan()

//@EnableDiscoveryClient
public class Demo1NacosApplication {

    public static void main(String[] args) {
        SpringApplication.run(Demo1NacosApplication.class, args);
    }

}
