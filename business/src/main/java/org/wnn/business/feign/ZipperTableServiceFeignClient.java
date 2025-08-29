package org.wnn.business.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.wnn.business.feign.dto.ZipperTableDTO;
import org.wnn.core.global.response.dto.CommonResponse;

/**
 * @author NanNan Wang
 */
@FeignClient(name = "bytekeep-portal",
        url = "${feign.url.zipper:}",
        path = "/api/public/zipper-table")
public interface ZipperTableServiceFeignClient {



    @PostMapping("/add")
    CommonResponse<Void> addZipperTable(@RequestBody ZipperTableDTO request);


}
