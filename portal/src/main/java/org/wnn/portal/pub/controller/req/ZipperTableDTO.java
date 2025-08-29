package org.wnn.portal.pub.controller.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.wnn.core.validation.CreateGroup;
import org.wnn.core.validation.DeleteGroup;
import org.wnn.core.validation.UpdateGroup;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;

/**
 *
 * @author NanNan Wang
 */
@Data
public class ZipperTableDTO {

    @NotEmpty(message = "拉链表名不能为空",groups = {CreateGroup.class, DeleteGroup.class, UpdateGroup.class})
    private String zipperTableName;

    @NotNull(message = "业务信息不能为空",groups = {CreateGroup.class, DeleteGroup.class})
    private Map<String, Object> businessKeyValues; // 业务键值对（如 {emp_id: 1001}）

    @NotNull(message = "生效时间不能为空",groups = {CreateGroup.class, UpdateGroup.class})
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate; // 新记录的生效开始时间

}
