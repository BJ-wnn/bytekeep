package org.wnn.portal.zipper.controller;

import org.wnn.portal.zipper.controller.dto.ZipperTableMetaDTO;
import org.wnn.portal.zipper.dao.entity.ZipperTableMeta;

/**
 * @author NanNan Wang
 */
class ZipperMetaConverter {

    // DTO转Entity（用于新增/更新）
    public static ZipperTableMeta toEntity(ZipperTableMetaDTO dto) {
        if (dto == null) {
            return null;
        }
        ZipperTableMeta entity = new ZipperTableMeta();
        entity.setId(dto.getId());
        entity.setZipperTableName(dto.getZipperTableName());
        entity.setZipperTableSelectSql(dto.getZipperTableSelectSql());
        entity.setZipperTableInsertSql(dto.getZipperTableInsertSql());
        entity.setZipperTableUpdateSql(dto.getZipperTableUpdateSql());
        entity.setZipperTableDeleteSql(dto.getZipperTableDeleteSql());
        entity.setBusinessTableName(dto.getBusinessTableName());
        entity.setBusinessTableInsertSql(dto.getBusinessTableInsertSql());
        entity.setBusinessTableUpdateSql(dto.getBusinessTableUpdateSql());
        entity.setBusinessTableDeleteSql(dto.getBusinessTableDeleteSql());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        return entity;
    }

    // Entity转DTO（用于查询返回）
    public static ZipperTableMetaDTO toDTO(ZipperTableMeta entity) {
        if (entity == null) {
            return null;
        }
        ZipperTableMetaDTO dto = new ZipperTableMetaDTO();
        dto.setId(entity.getId());
        dto.setZipperTableName(entity.getZipperTableName());
        dto.setZipperTableSelectSql(entity.getZipperTableSelectSql());
        dto.setZipperTableInsertSql(entity.getZipperTableInsertSql());
        dto.setZipperTableUpdateSql(entity.getZipperTableUpdateSql());
        dto.setZipperTableDeleteSql(entity.getZipperTableDeleteSql());
        dto.setBusinessTableName(entity.getBusinessTableName());
        dto.setBusinessTableInsertSql(entity.getBusinessTableInsertSql());
        dto.setBusinessTableUpdateSql(entity.getBusinessTableUpdateSql());
        dto.setBusinessTableDeleteSql(entity.getBusinessTableDeleteSql());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
