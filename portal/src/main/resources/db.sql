
DROP TABLE IF EXISTS zipper_table_meta;
CREATE TABLE zipper_table_meta (
   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
   zipper_table_name VARCHAR(100) NOT NULL COMMENT '拉链表表名（如job_info_zipper）',
   zipper_table_primary_key VARCHAR(100) NOT NULL COMMENT '拉链表主键字段名（如id）',
   zipper_table_select_sql TEXT NOT NULL COMMENT '查询历史记录的SQL模板，强制使用生效时间字段升序排序（ASC), 字段取值推荐使用驼峰',
   zipper_table_select_latest_sql TEXT NOT NULL COMMENT '查询拉链表最新版本的SQL模板',
   zipper_table_insert_sql TEXT NOT NULL COMMENT '插入历史记录的SQL模板,强制使用 INSERT INTO *** ON DUPLICATE KEY UPDATE ** 语法',
   zipper_table_update_sql TEXT NOT NULL COMMENT '更新历史记录的SQL模板',
   zipper_table_delete_sql TEXT NOT NULL COMMENT '删除历史记录的SQL模板，推荐使用物理删除 DELETE',
   business_table_name VARCHAR(100)  COMMENT '拉链表生效数据的业务表名',
   business_table_select_sql TEXT COMMENT '查询拉链表生效数据的SQL模板',
   business_table_insert_sql TEXT COMMENT '插入拉链表生效数据的SQL模板，,强制使用 INSERT INTO *** ON DUPLICATE KEY UPDATE ** 语法',
   business_table_update_sql TEXT COMMENT '更新拉链表生效数据的SQL模板',
   business_table_delete_sql TEXT COMMENT '删除拉链表生效数据的SQL模板',
   description VARCHAR(500) COMMENT '表描述信息',
   break_strategy TINYINT NOT NULL DEFAULT 0 COMMENT '断裂修复策略：0-删除节点后，前序节点失效时间=删除节点的失效时间；1-删除节点后，后序节点生效时间=删除节点的生效时间',
   status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
   created_by VARCHAR(50) NOT NULL COMMENT '创建人',
   created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   updated_by VARCHAR(50) COMMENT '更新人',
   updated_time TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   UNIQUE KEY uk_table_name (zipper_table_name) COMMENT '拉链表名唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拉链表元信息配置表，支持直接配置查询SQL模板';




# 测试用
INSERT INTO zipper_table_meta (
    zipper_table_name,
    zipper_table_primary_key,
    zipper_table_select_sql,
    zipper_table_select_latest_sql,
    zipper_table_insert_sql,
    zipper_table_update_sql,
    zipper_table_delete_sql,
    business_table_name,
    business_table_select_sql,
    business_table_insert_sql,
    business_table_update_sql,
    business_table_delete_sql,
    description,
    status,
    created_by
) VALUES (
             -- 拉链表名（必须与实际拉链表名一致）
             'job_info_zipper',
             'id' ,
             -- 拉链表查询模板：按业务键查询历史版本，按生效时间排序
             'SELECT id, job_code as jobCode, job_name as jobName, org_unit as orgUnit, job_grade as jobGrade, job_status as jobStatus, effective_date as effectiveDate,effective_end_date as effectiveEndDate
              FROM job_info_zipper
              WHERE job_code = #{params.jobCode} AND job_status = 1
              ORDER BY effective_date ASC',

             -- 拉链表查询模板: 按业务键查询最新版本
             'SELECT job_code as jobCode, job_name as jobName, org_unit as orgUnit, job_grade as jobGrade, job_status as jobStatus, effective_date as effectiveDate,effective_end_date as effectiveEndDate
                 FROM job_info_zipper
                 WHERE job_code = #{params.jobCode} AND job_status = 1 AND effective_date <= CURDATE() AND CURDATE() < effective_end_date
             ',

             -- 拉链表插入模板：新增历史版本（含生效/终止时间）
             'INSERT INTO job_info_zipper (
                 job_code, job_name, org_unit, job_grade, job_status,
                 effective_date, effective_end_date
              ) VALUES (
                 #{params.jobCode}, #{params.jobName}, #{params.orgUnit}, #{params.jobGrade}, #{params.jobStatus},
                 #{params.effectiveDate}, #{params.effectiveEndDate}
              )',

             -- 拉链表更新模板：终止旧版本（用于新增新版本时，关闭上一版本）
             'UPDATE job_info_zipper
              SET effective_date = #{params.effectiveDate},
                  effective_end_date = #{params.effectiveEndDate}
              WHERE id = #{params.id}',

             -- 拉链表删除模板：（谨慎使用）
             'DELETE FROM job_info_zipper
              WHERE id = #{params.id}',

             -- 关联的业务表名（当前生效表）
             'job_info',
             -- 业务表查询模板：按业务键查询当前生效版本
             '',

             -- 业务表插入模板：新增当前生效记录
             'INSERT INTO job_info (
                 job_code, job_name, org_unit, job_grade, job_status,  effective_status,update_date
              ) VALUES (
                 #{params.jobCode}, #{params.jobName}, #{params.orgUnit}, #{params.jobGrade}, #{params.jobStatus}, #{params.effectiveStatus},
                 CURDATE()
              ) ON DUPLICATE KEY UPDATE
                 job_name = #{params.jobName},
                 org_unit = #{params.orgUnit},
                 job_grade = #{params.jobGrade},
                 job_status = #{params.jobStatus},
                 effective_status = #{params.effectiveStatus},
                 update_date = CURDATE()
             ',

             -- 业务表更新模板：同步最新状态到当前表
             'UPDATE job_info
              SET job_name = #{params.jobName},
                  org_unit = #{params.orgUnit},
                  job_grade = #{params.jobGrade},
                  job_status = #{params.jobStatus},
                  update_date =  CURDATE()
              WHERE job_code = #{params.jobCode}',

             -- 业务表删除模板：删除当前生效记录（通常用于职位彻底删除）
             'DELETE FROM job_info
              WHERE job_code = #{params.jobCode}',

             -- 描述信息
             '职位信息拉链表配置：关联业务表job_info，存储职位所有历史版本，支持按时间追溯',

             -- 状态：1-启用
             1,

             -- 创建人（根据实际情况修改）
             'system'
         );


# 拉链表 职位信息例子
DROP TABLE IF EXISTS job_info;
CREATE TABLE job_info (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
                          job_code VARCHAR(50) NOT NULL COMMENT '职位编码（业务唯一键，如"DEV001"）',
                          job_name VARCHAR(100) NOT NULL COMMENT '职位名称（最新版本名称）',
                          org_unit VARCHAR(100) NOT NULL COMMENT '所属单位（最新版本所属）',
                          job_grade VARCHAR(20) COMMENT '职位等级（如"P3"）',
                          job_status TINYINT DEFAULT 1 COMMENT '职位状态（1-有效，0-失效，业务逻辑状态）',
                          update_date DATE NOT NULL COMMENT '最后更新日期（记录当前信息的更新时间）',
                          effective_status TINYINT NOT NULL DEFAULT 0 COMMENT '生效状态：0-未生效（未来），1-生效中，2-已失效',
                          UNIQUE KEY uk_job_code (job_code) COMMENT '职位编码唯一（每个职位仅一条最新记录）',
                          KEY idx_job_code_effective_status (job_code, effective_status) COMMENT '优化按职位+生效状态的查询',
                          KEY idx_effective_status (effective_status) COMMENT '优化按生效状态批量筛选（如查询所有未生效职位）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位信息当前表，存储每个职位的最新版本（含未生效、生效中状态）';



DROP TABLE IF EXISTS job_info_zipper;
CREATE TABLE job_info_zipper (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键（每条历史记录唯一标识）',
                                 job_code VARCHAR(50) NOT NULL COMMENT '职位编码（业务键，与当前表一致）',
                                 job_name VARCHAR(100) NOT NULL COMMENT '该版本的职位名称',
                                 org_unit VARCHAR(100) NOT NULL COMMENT '该版本的所属单位',
                                 job_grade VARCHAR(20) COMMENT '该版本的职位等级',
                                 job_status TINYINT DEFAULT 1 COMMENT '该版本的职位状态（1-有效，0-失效）',
                                 effective_date DATE NOT NULL COMMENT '生效开始日期（左闭，该版本从这一天开始有效）',
                                 effective_end_date DATE NOT NULL DEFAULT '9999-12-31' COMMENT '生效终止日期（右开，该版本在这一天及之后失效，默认9999-12-31表示当前有效）',
                                 INDEX idx_job_code_effdt (job_code, effective_date) COMMENT '优化按职位+时间查询历史版本的效率'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位信息拉链表，存储所有历史版本，支持时间区间追溯';

INSERT INTO job_info_zipper (
    job_code, job_name, org_unit, job_grade, job_status,
    effective_date, effective_end_date
) VALUES (
             'JOB001', 'Java开发工程师', '技术部', 'P3', 1,
             '2023-01-01', '9999-12-31'  -- 初始版本：从2023-01-01起有效
         );