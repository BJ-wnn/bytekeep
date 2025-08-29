package org.wnn.business.demos.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wnn.business.demos.controller.dto.JobDto;
import org.wnn.business.feign.ZipperTableServiceFeignClient;
import org.wnn.business.feign.dto.ZipperTableDTO;
import org.wnn.core.global.response.annotation.ResponseAutoWrap;
import org.wnn.core.global.response.dto.CommonResponse;
import org.wnn.core.validation.CreateGroup;

/**
 * @author NanNan Wang
 */
@RestController
@ResponseAutoWrap
@Slf4j
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobZipperTableManageController {

    private final ZipperTableServiceFeignClient zipperTableServiceFeignClient;

    @PostMapping("/add")
    public void addJob(@Validated({CreateGroup.class}) @RequestBody JobDto request) {
        final CommonResponse<Void> result = zipperTableServiceFeignClient.addZipperTable(
                new ZipperTableDTO()
                        .setZipperTableName("job_info_zipper")
                        .setEffectiveDate(request.getEffectiveDate())
                        .setBusinessKeyValues(request)
        );
        log.info("插入是否成功？{}" , result.getCode() == 200);
    }


}