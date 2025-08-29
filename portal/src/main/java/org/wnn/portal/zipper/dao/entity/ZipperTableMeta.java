package org.wnn.portal.zipper.dao.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author NanNan Wang
 */
/**
 * 拉链表元信息实体类（与数据库表zipper_table_meta映射）
 */
@Data
public class ZipperTableMeta {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 拉链表表名（如job_info_zipper）
     * 对应数据库字段：zipper_table_name
     */
    private String zipperTableName;

    /**
     * 拉链表主键字段名称
     */
    private String zipperTablePrimaryKey;

    /**
     * 拉链表查询SQL模板
     * 对应数据库字段：zipper_table_select_sql
     */
    private String zipperTableSelectSql;

    /**
     * 拉链表查询最新数据SQL模板
     */
    private String zipperTableSelectLatestSql;

    /**
     * 拉链表插入SQL模板
     * 对应数据库字段：zipper_table_insert_sql
     */
    private String zipperTableInsertSql;

    /**
     * 拉链表更新SQL模板
     * 对应数据库字段：zipper_table_update_sql
     */
    private String zipperTableUpdateSql;

    /**
     * 拉链表删除SQL模板
     * 对应数据库字段：zipper_table_delete_sql
     */
    private String zipperTableDeleteSql;


    /**
     * 关联的业务表名（当前生效数据的表，如job_info_current）
     * 对应数据库字段：business_table_name
     */
    private String businessTableName;

    /**
     * 业务表最新数据查询SQL模板
     * 对应数据库字段：business_table_select_sql
     */
    private String businessTableSelectSql;

    /**
     * 业务表插入SQL模板
     * 对应数据库字段：business_table_insert_sql
     */
    private String businessTableInsertSql;

    /**
     * 业务表更新SQL模板
     * 对应数据库字段：business_table_update_sql
     */
    private String businessTableUpdateSql;

    /**
     * 业务表删除SQL模板
     * 对应数据库字段：business_table_delete_sql
     */
    private String businessTableDeleteSql;

    /**
     * 断裂修复策略：0-删除节点后，前序节点失效时间=删除节点的失效时间；1-删除节点后，后序节点生效时间=删除节点的生效时间
     */
    private Integer breakStrategy;

    /**
     * 状态：1-启用，0-禁用（默认1）
     * 对应数据库字段：status
     */
    private Integer status = 1;

    /**
     * 表描述信息
     * 对应数据库字段：description
     */
    private String description;

    /**
     * 创建人
     * 对应数据库字段：created_by
     */
    private String createdBy;

    /**
     * 创建时间
     * 对应数据库字段：created_time
     */
    private LocalDateTime createdTime;

    /**
     * 更新人
     * 对应数据库字段：updated_by
     */
    private String updatedBy;

    /**
     * 更新时间
     * 对应数据库字段：updated_time
     */
    private LocalDateTime updatedTime;
}
