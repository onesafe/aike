package com._4paradigm.sage.aike.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by wangyiping on 2019/12/9 4:39 PM.
 */
@RestController
@RequestMapping("/aike/v1/dag")
public class DagController {

    @PostMapping
    public void upload(@RequestParam MultipartFile file) {

    }
}
