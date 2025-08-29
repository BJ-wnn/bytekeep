package org.wnn.portal.zipper.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wnn.core.global.response.annotation.ResponseAutoWrap;
import org.wnn.core.validation.CreateGroup;
import org.wnn.portal.zipper.controller.dto.ZipperTableMetaDTO;
import org.wnn.portal.zipper.dao.entity.ZipperTableMeta;
import org.wnn.portal.zipper.service.ZipperTableMetaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author NanNan Wang
 */
@RestController
@RequestMapping("/zipper-meta")
@RequiredArgsConstructor
@ResponseAutoWrap
public class ZipperTableMetaController {

    private final ZipperTableMetaService zipperTableMetaService;

//    /**
//     * 新增元信息配置（通过系统内部接口获取操作人）
//     */
//    @PostMapping("/add")
//    public Long add(@Validated(value = {CreateGroup.class}) @RequestBody ZipperTableMetaDTO dto) {
//        String operator = "wang"; // 从系统上下文获取当前操作人（替换为你的实际获取方式）
//
//        ZipperTableMeta entity = ZipperMetaConverter.toEntity(dto);
//        Long id = zipperTableMetaService.add(entity, operator);
//        return id;
//    }
//
//    /**
//     * 逻辑删除元信息（通过系统内部接口获取操作人）
//     */
//    @PostMapping("/delete/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        String operator = "wang"; //
//
//        zipperTableMetaService.delete(id, operator);
//        return ResponseEntity.noContent().build();
//    }
//
//    /**
//     * 更新元信息配置（通过系统内部接口获取操作人）
//     */
//    @PostMapping("/update")
//    public ResponseEntity<Void> update(@Validated @RequestBody ZipperTableMetaDTO dto) {
//        String operator = "wang"; //
//        ZipperTableMeta entity = ZipperMetaConverter.toEntity(dto);
//        zipperTableMetaService.update(entity, operator);
//        return ResponseEntity.noContent().build();
//    }
//
//    /**
//     * 根据ID查询
//     */
//    @GetMapping("/{id}")
//    public ResponseEntity<ZipperTableMetaDTO> getById(@PathVariable Long id) {
//        ZipperTableMeta entity = zipperTableMetaService.getById(id);
//        return entity != null
//                ? ResponseEntity.ok(ZipperMetaConverter.toDTO(entity))
//                : ResponseEntity.notFound().build();
//    }
//
//    /**
//     * 根据表名查询
//     */
//    @GetMapping("/table/{tableName}")
//    public ResponseEntity<ZipperTableMetaDTO> getByTableName(@PathVariable String tableName) {
//        ZipperTableMeta entity = zipperTableMetaService.getByTableName(tableName);
//        return entity != null
//                ? ResponseEntity.ok(ZipperMetaConverter.toDTO(entity))
//                : ResponseEntity.notFound().build();
//    }
//
//    /**
//     * 查询所有
//     */
//    @GetMapping("/all")
//    public ResponseEntity<List<ZipperTableMetaDTO>> getAll() {
//        List<ZipperTableMeta> entities = zipperTableMetaService.getAll();
//        List<ZipperTableMetaDTO> dtos = entities.stream()
//                .map(ZipperMetaConverter::toDTO)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(dtos);
//    }
//
//    /**
//     * 查询所有启用的元信息
//     */
//    @GetMapping("/enabled")
//    public ResponseEntity<List<ZipperTableMetaDTO>> getEnabled() {
//        List<ZipperTableMeta> entities = zipperTableMetaService.getEnabled();
//        List<ZipperTableMetaDTO> dtos = entities.stream()
//                .map(ZipperMetaConverter::toDTO)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(dtos);
//    }

}
