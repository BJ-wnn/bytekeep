package org.wnn.portal.menu.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author NanNan Wang
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/menu")
public class SysMenuController {


    @PostMapping("/list")
    public String getMenuList() {
        return "menu list";
    }

}
