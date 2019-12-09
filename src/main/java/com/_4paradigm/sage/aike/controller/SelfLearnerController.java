package com._4paradigm.sage.aike.controller;

import com._4paradigm.sage.aike.entity.SelfLearnerDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by wangyiping on 2019/12/9 4:23 PM.
 */
@RestController
@RequestMapping("/aike/v1/selflearner")
public class SelfLearnerController {

    @PostMapping
    public void createSelfLearner(@RequestBody SelfLearnerDTO selfLearnerDTO) {

    }
}
