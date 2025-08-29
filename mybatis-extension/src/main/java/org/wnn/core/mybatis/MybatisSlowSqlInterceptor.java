package org.wnn.core.mybatis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MyBatis SQL执行拦截器，用于监控并记录执行时间超过阈值的慢SQL。
 * <p>
 * 该拦截器通过MyBatis的插件机制，拦截{@link StatementHandler}的query、update、batch方法，
 * 计算SQL执行耗时，当耗时超过配置的阈值时，通过专用日志记录慢SQL信息。
 * 功能开关及参数可通过配置文件动态调整，无需修改代码。
 * </p>
 *
 * @author NanNan Wang
 * @date 2025-08-11
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})
})
public class MybatisSlowSqlInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger("slowSqlLog");

    /**
     * 慢SQL配置属性对象，通过{@link ConfigurationProperties}绑定配置文件参数
     */
    private final SlowSqlProperties slowSqlProperties;

    /**
     * 构造函数，注入慢SQL配置属性
     *
     * @param slowSqlProperties 慢SQL配置属性对象，包含阈值、服务名等参数
     */
    public MybatisSlowSqlInterceptor(SlowSqlProperties slowSqlProperties) {
        this.slowSqlProperties = slowSqlProperties;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 拦截SQL执行过程，计算执行耗时并判断是否为慢SQL
     *
     * @param invocation 拦截器调用对象，包含目标方法、参数等信息
     * @return 目标方法执行结果
     * @throws Throwable 执行过程中抛出的异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
//        // 获取 SQL 语句
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        Object parameterObject = statementHandler.getBoundSql().getParameterObject();
        String parameters = parameterObject != null ? parameterObject.toString() : "No parameters";

        // 计算 SQL 执行时间（毫秒）
        long start = System.nanoTime();
        // SQL执行
        Object result = invocation.proceed();
        long end = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(end - start);

        // 如果 SQL 执行时间超过阈值，记录慢 SQL
        if (durationMs > slowSqlProperties.getThreshold()) {
            // 构建SQL详细信息（嵌套对象）
            Map<String, Object> sqlInfo = new LinkedHashMap<>();
            sqlInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            sqlInfo.put("durationMs", durationMs);
            sqlInfo.put("sql", sql);
            sqlInfo.put("parameters", parameters);
            sqlInfo.put("threshold", slowSqlProperties.getThreshold());

            // 构建顶级日志对象
            Map<String, Object> logData = new LinkedHashMap<>();
            logData.put("service_name", slowSqlProperties.getServiceName());
            logData.put("sql_info", sqlInfo); // SQL信息作为嵌套字段

            // 直接将traceId作为顶级字段
            String traceId = MDC.get("traceId");
            if (traceId != null) {
                logData.put("traceId", traceId);
            }

            try {
                // 输出JSON格式日志
                String jsonLog = objectMapper.writeValueAsString(logData);
                logger.info(jsonLog);
            } catch (JsonProcessingException e) {
                // 序列化失败时回退到普通日志
                logger.info("Slow SQL (JSON serialization failed): duration={}ms, sql={}, parameters={}",
                        durationMs, sql, parameters);
            }
        }

        return result;
    }
    /**
     * 包装目标对象，生成MyBatis插件代理对象
     * <p>
     * 拦截器通过@Signature指定了只拦截StatementHandler的方法（query/update/batch），所以只需要对StatementHandler类型的对象创建代理。
     * 对其他类型的对象（如Executor、ParameterHandler等）直接返回原对象，避免不必要的代理，提高性能
     * </p>
     *
     * @param target 目标对象（此处为StatementHandler实例）
     * @return 包装后的代理对象，若无需拦截则返回原对象
     */
    @Override
    public Object plugin(Object target) {
        // 仅对StatementHandler类型的对象进行代理
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }



    /**
     * 慢SQL监控配置属性类，用于绑定配置文件中的参数。
     * <p>
     * 配置前缀为{@code slow.sql}，支持通过application.properties/yml进行配置，
     * 包含是否启用、阈值、服务名等核心参数。
     * </p>
     */
    @ConfigurationProperties(prefix = "slow.sql")
    public static class SlowSqlProperties {

        /**
         * 慢SQL判断阈值（单位：毫秒），默认值500ms
         */
        private long threshold = 500;

        /**
         * 服务名称，用于日志中区分不同服务的慢SQL，默认值"unknown-service"
         */
        private String serviceName = "unknown-service";

        /**
         * 是否启用慢SQL监控，默认关闭（false）
         */
        private boolean enabled = false;

        /**
         * 获取慢SQL阈值（毫秒）
         *
         * @return 阈值毫秒数
         */
        public long getThreshold() {
            return threshold;
        }

        /**
         * 设置慢SQL阈值（毫秒）
         *
         * @param threshold 阈值毫秒数（需为正数）
         */
        public void setThreshold(long threshold) {
            this.threshold = threshold;
        }

        /**
         * 获取服务名称
         *
         * @return 服务名称字符串
         */
        public String getServiceName() {
            return serviceName;
        }

        /**
         * 设置服务名称
         *
         * @param serviceName 服务名称（建议使用应用名，便于日志聚合）
         */
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        /**
         * 判断是否启用慢SQL监控
         *
         * @return true=启用，false=关闭
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * 设置是否启用慢SQL监控
         *
         * @param enabled 启用状态
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
