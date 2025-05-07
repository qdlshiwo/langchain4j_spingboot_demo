package com.kizzo.langchain4j_spingboot_demo.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan({"com.kizzo.langchain4j_spingboot_demo.mapper"})
@EnableTransactionManagement
public class MyBatisConfig {


}
