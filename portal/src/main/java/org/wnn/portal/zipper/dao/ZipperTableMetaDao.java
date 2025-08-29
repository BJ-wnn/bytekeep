package org.wnn.portal.zipper.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wnn.portal.zipper.dao.entity.ZipperTableMeta;

import java.util.List;

/**
 * @author NanNan Wang
 */
@Mapper
public interface ZipperTableMetaDao {


    ZipperTableMeta selectByTableName(@Param("zipperTableName") String zipperTableName);



//    /**
//     * 新增元信息配置
//     */
//    int insert(ZipperTableMeta meta);
//
//    /**
//     * 根据ID删除（逻辑删除，更新状态为禁用）
//     */
//    int deleteById(@Param("id") Long id, @Param("updatedBy") String updatedBy);
//
//    /**
//     * 更新元信息配置（全量更新）
//     */
//    int update(ZipperTableMeta meta);
//
//    /**
//     * 根据ID查询
//     */
//    ZipperTableMeta selectById(@Param("id") Long id);
//
//    /**
//     * 根据表名查询（用于唯一校验）
//     */
//    ZipperTableMeta selectByTableName(@Param("tableName") String tableName);
//
//    /**
//     * 查询所有元信息（支持分页，这里简化为全量查询）
//     */
//    List<ZipperTableMeta> selectAll();
//
//    /**
//     * 查询启用的元信息
//     */
//    List<ZipperTableMeta> selectEnabled();

}
