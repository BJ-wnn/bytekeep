package org.wnn.business.demos.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.wnn.core.validation.CreateGroup;
import org.wnn.core.validation.DeleteGroup;
import org.wnn.core.validation.UpdateGroup;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * @author NanNan Wang
 */
@Data
public class JobDto {

    @NotNull(message = "id不能为空", groups = {UpdateGroup.class, DeleteGroup.class})
    private Integer id;
    @NotEmpty(message = "职位名称不能为空",groups = {CreateGroup.class,UpdateGroup.class})
    private String jobName;
    @NotEmpty(message = "职位编码不能为空",groups = {CreateGroup.class,UpdateGroup.class})
    private String jobCode;
    @NotEmpty(message = "组织单元不能为空",groups = {CreateGroup.class,UpdateGroup.class})
    private String orgUnit;
    @NotEmpty(message = "职位等级不能为空",groups = {CreateGroup.class,UpdateGroup.class})
    private String jobGrade;
    @NotEmpty(message = "职位状态不能为空",groups = {CreateGroup.class,UpdateGroup.class})
    private String jobStatus;
    @NotNull(message = "生效日期不能为空",groups = {CreateGroup.class,UpdateGroup.class})
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

}
