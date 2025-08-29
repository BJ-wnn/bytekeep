//package org.wnn.core;
//
//import org.apache.ibatis.executor.statement.StatementHandler;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.wnn.core.mybatis.MybatisSlowSqlInterceptor;
//
///**
// * MyBatis慢SQL拦截器的自动配置类
// *
// * <p>
// * 该类基于Spring Boot自动配置机制，实现MyBatis慢SQL拦截器的自动装配。
// * 主要功能包括：
// * 1. 启用对{@link MybatisSlowSqlInterceptor.SlowSqlProperties}配置属性的绑定
// * 2. 根据配置条件（slow.sql.enabled=true）创建慢SQL拦截器Bean
// * </p>
// *
// * @author NanNan Wang
// * @since 1.0.0
// * @see MybatisSlowSqlInterceptor.SlowSqlProperties
// * @see MybatisSlowSqlInterceptor
// */
//@Configuration
//@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
//@ConditionalOnClass(StatementHandler.class)
//@EnableConfigurationProperties(MybatisSlowSqlInterceptor.SlowSqlProperties.class)
//@ConditionalOnProperty(prefix = "slow.sql", name = "enabled", havingValue = "true")
//public class MybatisSlowSqlAutoConfiguration {
//
//    private final MybatisSlowSqlInterceptor.SlowSqlProperties props;
//
//    public MybatisSlowSqlAutoConfiguration(MybatisSlowSqlInterceptor.SlowSqlProperties props) {
//        this.props = props;
//    }
//
//    /**
//     * 创建MyBatis慢SQL拦截器Bean
//     *
//     * <p>
//     * 该方法仅在配置项{@code slow.sql.enabled=true}时生效，
//     * 会将{@link MybatisSlowSqlInterceptor.SlowSqlProperties}配置属性注入到拦截器中，
//     * 用于实现对执行耗时超过阈值的SQL语句进行监控和记录。
//     * </p>
//     *
//     * @return 配置完成的MybatisSlowSqlInterceptor实例
//     */
//    @Bean
//    public MybatisSlowSqlInterceptor mybatisSlowSqlInterceptor() {
//        return new MybatisSlowSqlInterceptor(props);
//    }
//
//
//}
