package org.wnn.portal.zipper.controller.dto;

import lombok.Data;
import org.wnn.core.validation.CreateGroup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author NanNan Wang
 */
@Data
public class ZipperTableMetaDTO {

    private Long id;

    /**
     * 拉链表表名（如job_info_zipper）
     */
    @NotBlank(message = "拉链表名不能为空",groups = {CreateGroup.class})
    @Size(max = 100, message = "拉链表名长度不能超过100字符")
    private String zipperTableName;

    /**
     * 拉链表查询SQL模板
     * 示例：SELECT * FROM ${zipperTableName} WHERE job_code=#{zipperParams.jobCode}
     */
    @Size(max = 65535, message = "拉链表查询SQL模板长度不能超过65535字符")
    private String zipperTableSelectSql;


    /**
     * 拉链表插入SQL模板
     * 示例：INSERT INTO ${zipperTableName} (job_code, effective_date) VALUES (#{zipperParams.jobCode}, #{zipperParams.effectiveDate})
     */
    @Size(max = 65535, message = "拉链表插入SQL模板长度不能超过65535字符")
    private String zipperTableInsertSql;


    /**
     * 拉链表更新SQL模板
     * 示例：UPDATE ${zipperTableName} SET effective_end_date=#{zipperParams.effectiveEndDate} WHERE job_code=#{zipperParams.jobCode}
     */
    @Size(max = 65535, message = "拉链表更新SQL模板长度不能超过65535字符")
    private String zipperTableUpdateSql;

    /**
     * 拉链表删除SQL模板
     * 示例：DELETE FROM ${zipperTableName} WHERE id=#{zipperParams.id}
     */
    @Size(max = 65535, message = "拉链表删除SQL模板长度不能超过65535字符")
    private String zipperTableDeleteSql;

    /**
     * 关联的业务表名（当前生效数据的表，如job_info_current）
     */
    @Size(max = 100, message = "业务表名长度不能超过100字符")
    private String businessTableName;

    /**
     * 业务表插入SQL模板
     */
    @Size(max = 65535, message = "业务表插入SQL模板长度不能超过65535字符")
    private String businessTableInsertSql;

    /**
     * 业务表更新SQL模板
     */
    @Size(max = 65535, message = "业务表更新SQL模板长度不能超过65535字符")
    private String businessTableUpdateSql;

    /**
     * 业务表删除SQL模板
     */
    @Size(max = 65535, message = "业务表删除SQL模板长度不能超过65535字符")
    private String businessTableDeleteSql;

    private Integer status = 1; // 默认启用

    @Size(max = 500, message = "描述信息长度不能超过500字符")
    private String description;
}
