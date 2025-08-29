package org.wnn.portal.pub.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wnn.core.global.response.annotation.ResponseAutoWrap;
import org.wnn.core.validation.CreateGroup;
import org.wnn.core.validation.DeleteGroup;
import org.wnn.core.validation.UpdateGroup;
import org.wnn.portal.pub.controller.req.ZipperTableDTO;
import org.wnn.portal.pub.service.ZipperTableService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author NanNan Wang
 */
@RestController
@RequestMapping("/api/public/zipper-table")
@RequiredArgsConstructor
@Api(value = "拉链表公共管理功能")
@ResponseAutoWrap
@Slf4j
public class ZipperTableApiController {

    private final ZipperTableService zipperTableService;

    @PostMapping("/add")
    public void add(HttpServletRequest requestServlet, @Validated({CreateGroup.class}) @RequestBody ZipperTableDTO request) {
        String traceId = requestServlet.getHeader("X-Trace-Id"); // 或自定义的Header名称
        log.info("Received traceId: {}", traceId);
        zipperTableService.insert(request);
    }

    @PostMapping("/update")
    public void update(@Validated({UpdateGroup.class}) @RequestBody ZipperTableDTO request) {
        zipperTableService.update(request);
    }

    @PostMapping("/delete")
    public void delete(@Validated({DeleteGroup.class}) @RequestBody ZipperTableDTO request) {
        zipperTableService.delete(request);
    }


}
