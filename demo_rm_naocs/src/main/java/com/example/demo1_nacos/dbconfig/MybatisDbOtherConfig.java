package com.example.demo1_nacos.dbconfig;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/9 11:05
 */
//@Configuration
//@MapperScan(basePackages = "com.example.demo1_nacos.mapper.sqlserver", sqlSessionFactoryRef = "otherSqlSessionFactory")
public class MybatisDbOtherConfig {

    //这里需要注意下：spring.datasource.other中，other名称是application.yml数据库配置的数据源名称
    @Bean(name = "otherDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.other")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "otherTransactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("otherDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);

    }

    @Bean(name = "otherSqlSessionFactory")
    public SqlSessionFactory basicSqlSessionFactory(@Qualifier("otherDataSource") DataSource basicDataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(basicDataSource);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:mapper/sqlserver/*.xml"));
        return factoryBean.getObject();
    }

    @Bean(name = "otherSqlSessionTemplate")
    public SqlSessionTemplate testSqlSessionTemplate(
            @Qualifier("otherSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
