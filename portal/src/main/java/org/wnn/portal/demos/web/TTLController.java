package org.wnn.portal.demos.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wnn.portal.demos.web.serivce.OrderService;

/**
 * @author NanNan Wang
 */
@RestController
@RequestMapping("/ttl")
@RequiredArgsConstructor
@Slf4j
public class TTLController {

    private final OrderService orderService;

    @GetMapping("/testAsync")
    public void testAsync() {
        log.info("start testAsync");
        orderService.handleOrderAsync(1L);
    }


    @GetMapping("/testTask")
    public void testTask() {
        log.info("start testTask");
        orderService.handleOrderWithCF(2L);
    }
}
