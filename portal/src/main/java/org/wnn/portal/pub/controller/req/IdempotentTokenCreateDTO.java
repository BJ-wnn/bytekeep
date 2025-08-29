package org.wnn.portal.pub.controller.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 获取幂等Token请求参数
 * @author NanNan Wang
 */

@Data
@ApiModel(value = "获取幂等Token请求参数")
public class IdempotentTokenCreateDTO {

    @ApiModelProperty(value = "接口名称，不能为空")
    @NotEmpty(message = "幂等Token请求参数 【接口名称】 不能为空")
    private String interfaceName;

    @ApiModelProperty(value = "接口参数字符串")
    @NotEmpty(message = "幂等Token请求参数 【接口参数】 不能为空")
    private String interfaceParamStr;

    @ApiModelProperty(value = "幂等Token有效期（秒），默认30秒")
    private Long expireSeconds;

}
