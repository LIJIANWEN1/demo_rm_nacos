package com.example.demo1_nacos.dbconfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import javax.sql.DataSource;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/9 11:05
 */
//@Configuration
//@MapperScan(basePackages ="com.example.demo1_nacos.mapper.mysql", sqlSessionFactoryRef = "masterSqlSessionFactory")
public class MybatisDbMasterConfig {

//    @Primary
    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    //这里需要注意下：spring.datasource.master中，master名称是application.yml数据库配置的数据源名称
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

//    @Primary
    @Bean(name = "masterSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("masterDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
//        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean(); 导致mybatis-plus_ 无法使用
        factoryBean.setDataSource(dataSource);
        factoryBean.setTypeAliasesPackage("com.example.demo1_nacos.pojo");
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:mapper/mysql/*.xml"));
        return factoryBean.getObject();
    }

//    @Primary
    @Bean(name = "masterTransactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("masterDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "masterSqlSessionTemplate")
//    @Primary
    public SqlSessionTemplate testSqlSessionTemplate(
            @Qualifier("masterSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}

