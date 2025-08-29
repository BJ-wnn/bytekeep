package org.wnn.business.feign.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 *
 *
 * @author NanNan Wang
 */
@Data
@Accessors(chain = true)
public class ZipperTableDTO<T> {

    private String zipperTableName; // 拉链表名
    private LocalDate effectiveDate;    // 生效时间
    private T businessKeyValues;    // 业务信息，不支持List等集合对象

}

